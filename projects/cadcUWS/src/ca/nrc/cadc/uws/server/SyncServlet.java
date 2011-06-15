/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2009.                            (c) 2009.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 *  $Revision: 4 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.uws.server;

import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.uws.ExecutionPhase;
import ca.nrc.cadc.uws.Job;
import ca.nrc.cadc.uws.JobAttribute;
import ca.nrc.cadc.uws.JobInfo;
import ca.nrc.cadc.uws.JobManager;
import ca.nrc.cadc.uws.JobPersistence;
import ca.nrc.cadc.uws.JobPersistenceException;
import ca.nrc.cadc.uws.JobRunner;
import ca.nrc.cadc.uws.JobWriter;
import ca.nrc.cadc.uws.Parameter;
import ca.nrc.cadc.uws.SyncJobRunner;
import ca.nrc.cadc.uws.SyncOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import javax.security.auth.Subject;
import javax.servlet.ServletConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Servlet that runs a SyncJobRunner for each request. This servlet supports both
 * GET and POST, creates and persists a jobm and issues a redirect to cause execution.
 * </p><p>
 * This servlet requires 3 context params to be set to specify the class names that implement
 * the 3 required interfaces. The <code>param-name</code> specifies the interface and
 * the <code>param-value</code> is the class name that implements the interface.
 * These context params are used by both the SyncServlet and the ASync support; as a
 * result, the JobRunner implementation still needs to implement SyncJobRunner, but is configured
 * using just the JobRunner interface name.
 * For example:
 * </p><p>
 * <pre>
 *      <context-param>
 *          <param-name>ca.nrc.cadc.uws.JobManager</param-name>
 *          <param-value>ca.nrc.cadc.uws.BasicJobManager</param-value>
 *      </context-param>
 *
 *      <context-param>
 *          <param-name>ca.nrc.cadc.uws.JobPersistence</param-name>
 *          <param-value>ca.nrc.cadc.uws.InMemoryPersistence</param-value>
 *      </context-param>
 *
 *      <context-param>
 *          <param-name>ca.nrc.cadc.uws.JobRunner</param-name>
 *          <param-value>com.example.MyJobRunner</param-value>
 *      </context-param>
 * </pre>
 *
 * @author pdowler
 */
public class SyncServlet extends HttpServlet
{
    private static Logger log = Logger.getLogger(SyncServlet.class);
    private static final long serialVersionUID = 201009291100L;
    private static final String JOB_EXEC = "run";
    private static final String TEXT_XML = "text/xml";
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    
    private JobManager jobManager;
    private JobPersistence jobPersistence;
    private Class jobRunnerClass;
    private boolean execOnGET = false;
    private boolean execOnPOST = false;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        String pname = null;
        String cname;

        try
        {
            String str = config.getInitParameter(SyncServlet.class.getName() + ".execOnGET");
            if (str !=null)
                try { execOnGET = Boolean.parseBoolean(str); }
                catch(Exception ignore) { }
            str = config.getInitParameter(SyncServlet.class.getName() + ".execOnPOST");
            if (str !=null)
                try { execOnPOST = Boolean.parseBoolean(str); }
                catch(Exception ignore) { }
            log.info("execOnGET: " + execOnGET);
            log.info("execOnPOST: " + execOnPOST);

            pname = JobManager.class.getName();
            //cname = config.getInitParameter(pname);
            cname = config.getServletContext().getInitParameter(pname);
            if (cname != null && cname.trim().length() > 0)
            {
                Class c = Class.forName(cname);
                this.jobManager = (JobManager) c.newInstance();
                log.info("created JobManager: " + jobManager.getClass().getName());
            }
            else
                log.error("required init-param not found: " + pname);

            pname = JobPersistence.class.getName();
            //cname = config.getInitParameter(pname);
            cname = config.getServletContext().getInitParameter(pname);
            if (cname != null )
            {
                Class c = Class.forName(cname);
                this.jobPersistence = (JobPersistence) c.newInstance();
                log.info("created JobPersistence: " + jobPersistence.getClass().getName());
                if (jobManager != null)
                    jobManager.setJobPersistence(jobPersistence);
            }
            else
                log.error("required init-param not found: " + pname);

            pname = JobRunner.class.getName();
            //cname = config.getInitParameter(pname);
            cname = config.getServletContext().getInitParameter(pname);
            if (cname != null )
            {
                Class c = Class.forName(cname);
                if (SyncJobRunner.class.isAssignableFrom(c))
                {
                    this.jobRunnerClass = c;
                    log.info("loaded JobRunner class: " + jobRunnerClass.getName());
                }
                else
                    log.error(cname + " does not implement " + SyncJobRunner.class.getName());
            }
            else
                log.error("required init-param not found: " + pname);
        }
        catch(Exception ex)
        {
            log.error("failed to create: " + pname +": " + ex);
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log.debug("doGet - START");
        doit(execOnGET, request, response);
        log.debug("doGet - DONE");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log.debug("doPost - START");
        doit(execOnPOST, request, response);
        log.debug("doPost - DONE");
    }

    private void doit(boolean execOnCreate, HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log.warn("doit: execOnCreate=" + execOnCreate);
        SyncRunner syncRunner = null;
        Subject subject = null;
        String jobID = null;
        Job job = null;
        String action = null;
        try
        {
            subject = getSubject(request);
            jobID = getJobID(request);
            if (jobID == null)
            {
                // create
                job = create(request, subject);
                log.debug(String.format("persisting job: ip:[%s] path:[%s]", job.getRequesterIp(), job.getRequestPath()));
                job = jobManager.persist(job);
                log.debug("persisted job: " + job);
                jobID = job.getID();

                String jobURL = getJobURL(request, job.getID());
                log.info("created job: " + jobURL);
                if (execOnCreate)
                {
                    log.info("no redirect, action = " + JOB_EXEC);
                    action = JOB_EXEC;
                }
                else // redirect
                {
                    String execURL = jobURL + "/" + JOB_EXEC;
                    log.info("redirect: " + execURL);
                    response.setHeader("Location", execURL);
                    response.setStatus(HttpServletResponse.SC_SEE_OTHER);
                    return;
                }
            }
            else
                // get job from persistence
                job = jobManager.getJob(jobID);
            
            if (job == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                PrintWriter w = response.getWriter();
                w.println("failed to find " + jobID);
                w.close();
                return;
            }
            log.debug("found: " + jobID);

            if (action == null)
                action = getJobAction(request);

            if (action == null)
            {
                log.info("dumping job: " + jobID);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/xml");
                JobWriter w = new JobWriter();
                w.write(job, new SafeOutputStream(response.getOutputStream()));
                return;
            }

            if ( !JOB_EXEC.equals(action) ) // this is the only valid action
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                PrintWriter w = response.getWriter();
                w.println("not found: " + jobID + "/" + action);
                w.close();
                return;
            }

            // authorization check: subject == owner (creator)
            Subject owner = job.getOwner();
            if (false && owner != null)
            {
                boolean ok = owner.getPrincipals().isEmpty(); // empty Subject == no owner
                if (subject != null)
                {
                    for (Principal p1 : owner.getPrincipals())
                       for (Principal p2 : subject.getPrincipals())
                           ok = ok || AuthenticationUtil.equals(p1, p2);
                }
                if (!ok)
                {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("text/plain");
                    PrintWriter w = response.getWriter();
                    w.println("access denied: job " + jobID);
                    w.close();
                    return;
                }
            }

            // check phase==PENDING
            if (!ExecutionPhase.PENDING.equals(job.getExecutionPhase()))
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                PrintWriter w = response.getWriter();
                w.println("synchronous job " + jobID + " has already been started");
                w.close();
                return;
            }

            log.info("executing job: " + jobID);
            
            // execute job
            SyncJobRunner jobRunner = (SyncJobRunner) jobRunnerClass.newInstance();
            jobRunner.setJob(job);
            jobRunner.setJobManager(jobManager);

            syncRunner = new SyncRunner(jobRunner, response);
            if (subject == null)
            {
                syncRunner.run();
            }
            else
            {
                Subject.doAs(subject, syncRunner);
            }
            
        }
        catch(JobPersistenceException pex)
        {
            String msg = "";
            if (jobID == null)
                msg = "failed to create new job";
            else
                msg = "failed to execute job + " + jobID;
            log.error(msg, pex);

            if (syncRunner != null)
            {
                SyncOutputImpl soi = syncRunner.getOut();
                if ( soi.isOpen() )
                {
                    log.error("failure after OutputStream opened, cannot report error to user");
                    return;
                }
            }
            // OutputStream not open, write an error response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");
            PrintWriter w = response.getWriter();
            w.println(msg);
            w.close();
            return;

        }
        catch(Throwable t)
        {
            if (jobID == null)
                log.error("create job failed", t);
            else
                log.error("execute job failed", t);
            if (syncRunner != null)
            {
                SyncOutputImpl soi = syncRunner.getOut();
                if ( soi.isOpen() )
                {
                    log.error("unexpected failure after OutputStream opened", t);
                    return;
                }
            }
            // OutputStream not open, write an error response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");
            PrintWriter w = response.getWriter();
            w.println("job " + jobID + " failed unexpectedly: ");
            t.printStackTrace(w);
            w.close();
            return;
        }
        finally
        {
            if (syncRunner != null)
            {
                SyncOutputImpl soi = syncRunner.getOut();
                if (soi.isOpen())
                    try 
                    {
                        OutputStream ostream = soi.getOutputStream();
                        ostream.flush();
                    }
                    catch(Throwable ignore) { }
            }
        }
    }

    private class SyncRunner implements PrivilegedAction<Object>
    {
        SyncJobRunner runner;
        HttpServletResponse response;
        SyncOutputImpl syncOutput;
        
        SyncRunner(SyncJobRunner runner, HttpServletResponse response)
        {
            this.runner = runner;
            this.response = response;
            this.syncOutput = new SyncOutputImpl(response);
        }

        SyncOutputImpl getOut() { return syncOutput; }

        @Override
        public Object run()
        {
            Job j = runner.getJob();
            String jobID = j.getID();

            j.setExecutionPhase(ExecutionPhase.QUEUED);
            j = jobManager.persist(j);

            runner.setJob(j);

            URL redirectURL = runner.getRedirectURL();
            if (redirectURL != null)
            {
                String loc = redirectURL.toExternalForm();
                log.debug("redirect from JobRunner: "+ loc);
                response.setHeader("Location", loc);
                response.setStatus(HttpServletResponse.SC_SEE_OTHER);
                return null;
            }

            // streaming
            runner.setOutput(syncOutput);
            runner.run();
            
            return null;
        }
    }

    private class SyncOutputImpl implements SyncOutput
    {
        OutputStream ostream;
        HttpServletResponse response;

        SyncOutputImpl(HttpServletResponse response)
        {
            this.response = response;
        }

        public boolean isOpen() { return ostream != null; }
        
        @Override
        public OutputStream getOutputStream()
            throws IOException
        {
            if (ostream == null)
            {
                log.debug("opening OutputStream");
                ostream = new SafeOutputStream(response.getOutputStream());
            }
            return ostream;
        }

        @Override
        public void setHeader(String key, String value)
        {
            if (ostream == null) // header not committed
                response.setHeader(key, value);
            else
                log.warn("setHeader: " + key + " = " + value + " AFTER OutputStream opened, ignoring");
        }
    }

    private Subject getSubject(HttpServletRequest request)
    {
        String remoteUser = request.getRemoteUser();
        X509Certificate[] ca = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        Collection<X509Certificate> certs = null;
        if (ca != null && ca.length > 0) certs = Arrays.asList(ca);
        return AuthenticationUtil.getSubject(remoteUser, certs);
    }

    private String getJobID(HttpServletRequest request)
    {
        String path = request.getPathInfo();
        log.debug("path: " + path);
        // path can be null, <jobID> or <jobID>/exec
        if (path == null)
            return null;
        if (path.startsWith("/"))
            path = path.substring(1);
        String[] parts = path.split("/");
        log.debug("path: " + path + " jobID: " + parts[0]);
        return parts[0];
    }

    private String getJobAction(HttpServletRequest request)
    {
        String path = request.getPathInfo();
        log.debug("path: " + path);
        // path can be null, <jobID> or <jobID>/<token>
        if (path == null)
            return null;
        if (path.startsWith("/"))
            path = path.substring(1);
        String[] parts = path.split("/");
        String ret = null;
        if (parts.length == 2)
            ret = parts[1];
        log.debug("path: " + path + " jobAction: " + ret);
        return ret;
    }

    private Job create(HttpServletRequest request, Subject subject)
        throws IOException, JDOMException, ParseException
    {        
        // TODO: check content-type for params (www-urlencoded?) vs XML (text/xml)
        String contentType = request.getHeader("Content-Type");
        
        // pdowler: assume FORM_URLENCODED if not specified to support HTTP GET
        //if (contentType == null || !contentType.equals(TEXT_XML) && !contentType.equals(FORM_URLENCODED))
        //    throw new IllegalArgumentException("Content-Types must be " + TEXT_XML + " or " + FORM_URLENCODED);

        // Job from POSTed XML
        Job job = new Job();
        if (contentType != null && contentType.equals(TEXT_XML))
        {
            // Check that the XML is valid.
            SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
            Document doc = builder.build(request.getInputStream());
            StringWriter sw = new StringWriter();
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getCompactFormat());
            outputter.output(doc.detachRootElement(), sw);
            JobInfo jobInfo = new JobInfo(sw.toString(), TEXT_XML, true);
            job.setJobInfo(jobInfo);
            log.debug(jobInfo);
        }
                
        // Job from GET or POST parameters
        else
        {
            Enumeration<String> paramNames = request.getParameterNames();
            while ( paramNames.hasMoreElements() )
            {
                String p = paramNames.nextElement();

                if (JobAttribute.isValue(p))
                {
                    if ( JobAttribute.RUN_ID.getAttributeName().equalsIgnoreCase(p) )
                        job.setRunID(request.getParameter(p));
                }
                else
                {
                    String[] vals = request.getParameterValues(p);
                    if (vals != null)
                    {
                        for (String v : vals)
                        {
                            job.addParameter(new Parameter(p, v));
                        }
                    }
                }
            }
        }

        job.setOwner(subject);
        try
        {
            URL u = new URL(request.getRequestURL().toString());
            job.setRequestPath(u.getPath());
        }
        catch(MalformedURLException oops)
        {
            log.error("failed to get request path", oops);
        }
        
        String requesterIp = request.getRemoteAddr();
        job.setRequesterIp(requesterIp);

        // TODO: where to get Map of ns:<url to xsd file> for use with XmlUtil??

        return job;
    }

    private String getJobURL(HttpServletRequest request, String jobID)
    {
        StringBuffer sb = request.getRequestURL();
        log.debug("request URL: " + sb);
        if ( sb.charAt(sb.length()-1) != '/' )
            sb.append("/");
        sb.append(jobID);
        return sb.toString();
    }

    private class SafeOutputStream extends FilterOutputStream
    {
        SafeOutputStream(OutputStream ostream) { super(ostream); }

        @Override
        public void close()
            throws IOException
        {
            // must not let the JobRunner call close on the OutputStream!!!
        }
    }
}
