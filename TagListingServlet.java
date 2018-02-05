/**
 * Copyright (c)  Abbott Laboratories
 * Program Name :  EmailSendingServlet.java
 * Application  :  ADC Abbott Innovation
 * Purpose      :  See description
 * Description  :  Servlet thats get called when user submit the refer a friend form
 * Names of Databases accessed: JCR 
 * Modification History:
 * ---------------------
 *    Date                                Modified by                                       Modification Description
 *-----------                            ----------------                                    -------------------------
 *  17-APR-2017                  Cognizant Technology solutions                                 Initial Creation
 *-----------                           ----------------                                      -------------------------
 */

package com.cts.testing.servlet;

import java.io.IOException;
import java.rmi.ServerException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.cq.social.calendar.client.api.Page;
import com.cts.testing.service.email.SendEmailServiceImpl;
import com.day.cq.commons.RangeIterator;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import java.util.Iterator;
import java.util.UUID;

@Component(immediate = true, metatype = true)
@Service
@Properties({ @Property(name = "service.description", value = "Get the tag list - CTS Testing "),
	@Property(name = "service.vendor", value = "CTS"),
	@Property(name = "sling.servlet.paths", value = "/bin/servlet/cts/testing/gettaglist"),
	// Generic handler for all get requests
	@Property(name = "sling.servlet.methods", value = "POST", propertyPrivate = true) })
/**
 *  This servlets helps in sending an email
 *
 */
public class TagListingServlet extends SlingAllMethodsServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(TagListingServlet.class);
	@Reference
	private ConfigurationAdmin configAdmin;

	@Reference
	private ResourceResolverFactory resolverFactory;
	
	/**
	 * This method is executed when user submits the form from frontend
	 */
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServerException, IOException {
		LOG.debug("Executing doPOST-->TagListingServlet Serlvet");
		try {
			execute(request, response);
			LOG.debug("Execute method completed");
		} catch (Exception e) {
			LOG.error(e.getMessage());

		}
	}

	/**
	 * 
	 * @param slingRequest
	 * @param slingResponse
	 * @throws ServletException
	 * @throws IOException
	 * 
	 *             This method sends an email when user submit the form
	 * @throws RepositoryException 
	 * @throws JSONException 
	 * 
	 */
	protected void execute(SlingHttpServletRequest slingRequest, SlingHttpServletResponse slingResponse)
			throws ServletException, IOException, RepositoryException, JSONException {
		
		String rootPagePath = slingRequest.getParameter("rootPagePath");
		String rootTag = slingRequest.getParameter("rootTag");
		LOG.info("TagListingServlet --> execute started");
		
		ResourceResolver resolver = slingRequest.getResourceResolver();
		

		TagManager tagManager = slingRequest.getResourceResolver().adaptTo(TagManager.class);

		
		Resource resource = resolver.getResource(rootPagePath);
		/* List all the sub tags of root tag */
		Tag tags = resolver.getResource(rootTag).adaptTo(Tag.class);
		Iterator<Tag> allSubTags = tags.listChildren();
		JSONArray tagListArray = new JSONArray();
		while(allSubTags.hasNext()){
			JSONObject tagListObj = new JSONObject();
			Tag childTag = allSubTags.next();
			String tagArray[]= new String[1];
			tagArray[0] = childTag.getTagID();
			String childTagName = childTag.getTitle().toString();
			String childTagPath = childTag.getPath().toString();
			LOG.info("Child Tag Name : "+childTagName+" - > Child Tag Path : "+childTagPath);
			RangeIterator<Resource> pageList = tagManager.find(rootPagePath,tagArray);
			
			LOG.info("Page List Count : "+pageList.getSize());
			
            String thePath="" ; 
            tagListObj.put("tagName",childTagName);
            String pagePath = "";
            String pageTitle = "";
            String pageDescription = "";
            JSONArray childTagListArray = new JSONArray();
			 while (pageList.hasNext())
	            { 
				 	JSONObject childTagListObj = new JSONObject();
	                Resource myResource = (Resource)pageList.next(); 
	                thePath = myResource.getPath(); 
	                LOG.info("**** PATH is "+thePath);
	                Node pageNode = resolver.getResource(thePath).adaptTo(Node.class);
	                LOG.info(" Page Title : "+pageNode.getProperty("jcr:title").getString());
	                LOG.info(" Page Description : "+pageNode.getProperty("jcr:description").getString());
	                LOG.info(" Parent Page Path : "+pageNode.getParent().getPath());
	                pageTitle = pageNode.getProperty("jcr:title").getString();
	                pageDescription = pageNode.getProperty("jcr:description").getString();
	                pagePath = pageNode.getParent().getPath();
	                childTagListObj.put("pageTitle", pageTitle);
	                childTagListObj.put("pageDescription", pageDescription);
	                childTagListObj.put("pagePath", pagePath);
	                childTagListArray.put(childTagListObj);
	          
	            }
			 tagListObj.put("childPageList", childTagListArray);
			 tagListArray.put(tagListObj);
		}
		
		JSONObject mainObj = new JSONObject();
		mainObj.put("tagList", tagListArray);
		
		/*Tag[] allTags = tagManager.getTagsForSubtree(resource, false);

		for(Tag tag : allTags){
			
			String currentTag = tag.getTitle();
			LOG.info("Tag Title : "+currentTag);
		} */

		LOG.info("End of Servlet");

		slingResponse.getWriter().write(mainObj.toString());
	}


}
