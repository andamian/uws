/******************************************************************************
 *
 *  Copyright (C) 2009                          Copyright (C) 2009
 *  National Research Council           Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6                     Ottawa, Canada, K1A 0R6
 *  All rights reserved                         Tous droits reserves
 *
 *  NRC disclaims any warranties,       Le CNRC denie toute garantie
 *  expressed, implied, or statu-       enoncee, implicite ou legale,
 *  tory, of any kind with respect      de quelque nature que se soit,
 *  to the software, including          concernant le logiciel, y com-
 *  without limitation any war-         pris sans restriction toute
 *  ranty of merchantability or         garantie de valeur marchande
 *  fitness for a particular pur-       ou de pertinence pour un usage
 *  pose.  NRC shall not be liable      particulier.  Le CNRC ne
 *  in any event for any damages,       pourra en aucun cas etre tenu
 *  whether direct or indirect,         responsable de tout dommage,
 *  special or general, consequen-      direct ou indirect, particul-
 *  tial or incidental, arising         ier ou general, accessoire ou
 *  from the use of the software.       fortuit, resultant de l'utili-
 *                                                              sation du logiciel.
 *
 *
 *  This file is part of cadcUWS.
 *
 *  cadcUWS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  cadcUWS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with cadcUWS.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package ca.nrc.cadc.uws.web.restlet.resources;

import org.restlet.resource.ServerResource;
import org.restlet.data.Reference;
import org.apache.log4j.Logger;
import ca.nrc.cadc.uws.JobManager;
import ca.nrc.cadc.uws.InvalidServiceException;
import ca.nrc.cadc.uws.JobPersistence;
import ca.nrc.cadc.uws.util.StringUtil;
import ca.nrc.cadc.uws.web.validators.FormValidator;


/**
 * Base XML Resource for UWS Resources.
 */
public abstract class UWSResource extends ServerResource
{
    private static final Logger LOGGER = Logger.getLogger(UWSResource.class);

    protected final static String UWS_EXECUTOR_SERVICE =
            "ca.nrc.cadc.uws.JobExecutor";
    protected final static String UWS_JOB_SERVICE =
            "ca.nrc.cadc.uws.JobManager";
    protected final static String UWS_PERSISTENCE =
            "ca.nrc.cadc.uws.JobPersistence";
    protected final static String UWS_RUNNER = "ca.nrc.cadc.uws.JobRunner";

    protected final static String XML_NAMESPACE_PREFIX = "uws";
    protected final static String XML_NAMESPACE_URI = 
            "http://www.ivoa.net/xml/UWS/v1.0rc3";

    protected FormValidator formValidator;
    protected JobManager jobManager;


    /**
     * Constructor.
     */
    protected UWSResource()
    {

    }


    /**
     * Build the host portion of any outgoing URL that will be intended for a
     * local call.  This is useful when building XML and wanting to call upon
     * a local Resource to build a portion of it.
     *
     * An example would look like: http://myhost/context
     *
     * @return      String Host part of a URI.
     */
    protected String getHostPart()
    {
        final StringBuilder elementURI = new StringBuilder(128);
        final Reference ref = getRequest().getResourceRef();

        elementURI.append(ref.getSchemeProtocol().getSchemeName());
        elementURI.append("://");
        elementURI.append(ref.getHostDomain());
        elementURI.append(getContextPath());

        return elementURI.toString();
    }

    /**
     * Obtain the equivalent of the Servlet Context Path.  This is usually
     * the context of the current web application, or the part of the URL
     * that comes after the host:port.
     *
     * In the example of http://myhost/myapp, this method would return
     * /myapp.
     *
     * @return      String Context Path.
     */
    protected String getContextPath()
    {
        final Reference ref = getRequest().getResourceRef();
        final String[] pieces = ref.getPath().split("/");
        final String pathPrepend;

        if (!ref.getPath().startsWith("/"))
        {
            pathPrepend = "/" + pieces[0];
        }
        else
        {
            pathPrepend = "/" + pieces[1];
        }

        return pathPrepend;
    }

    /**
     * Obtain this Resource's Job Service.
     * @return  JobService instance, or null if none set.
     */
    protected JobManager getJobManager()
    {
        if (jobManager == null)
        {
            setJobManager(createJobManager());
        }

        return jobManager;
    }

    protected void setJobManager(final JobManager jobManager)
    {
        this.jobManager = jobManager;
    }

    public FormValidator getFormValidator()
    {
        return formValidator;
    }

    public void setFormValidator(final FormValidator formValidator)
    {
        this.formValidator = formValidator;
    }

    /**
     * Obtain a new instance of the Job Manager interface as defined in the
     * Context.
     *
     * @return  The JobManager instance.
     */
    @SuppressWarnings("unchecked")
    protected JobManager createJobManager()
    {
        final JobManager jobManager =
                (JobManager) createBean(UWS_JOB_SERVICE, true);
        jobManager.setJobPersistence(createJobPersistence());

        return jobManager;
    }

    /**
     * Obtain a new instance of the Job Manager interface as defined in the
     * Context.
     *
     * @return  The JobManager instance.
     */
    @SuppressWarnings("unchecked")
    protected JobPersistence createJobPersistence()
    {
        return (JobPersistence) createBean(UWS_PERSISTENCE, true);
    }

    protected Object createBean(final String contextBeanName,
                                final boolean mandatory)
    {
        if (mandatory && !StringUtil.hasText(
                getContext().getParameters().getFirstValue(contextBeanName)))
        {
            throw new InvalidServiceException(
                    "This bean is mandatory!\n\n Please set the "
                    + contextBeanName + "context-param in the web.xml, "
                    + "or insert it into the Context manually.");
        }

        final String clazz = getContext().getParameters().
                getFirstValue(contextBeanName);

        try
        {
            return Class.forName(clazz).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.error("No such bean >> " + clazz, e);
            throw new InvalidServiceException("No such bean >> " + clazz, e);
        }
        catch (IllegalAccessException e)
        {
            LOGGER.error("Class or Constructor is inaccessible for "
                         + "bean  >> " + clazz, e);
            throw new InvalidServiceException("Class or Constructor is "
                                              + "inaccessible for bean "
                                              + ">> " + clazz, e);
        }
        catch (InstantiationException e)
        {
            LOGGER.error("Cannot create bean instance >> "
                         + clazz, e);
            throw new InvalidServiceException("Cannot create bean instance >> "
                                              + clazz, e);
        }
    }
}
