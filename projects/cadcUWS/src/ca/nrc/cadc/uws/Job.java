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


package ca.nrc.cadc.uws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.security.auth.Subject;


/**
 * Default implementation of a Job.
 */
public class Job
{
    private String jobID;
    private ExecutionPhase executionPhase;
    private long executionDuration;
    private Date destructionTime;
    private Date quote;
    private Date startTime;
    private Date endTime;    
    private ErrorSummary errorSummary;
    private Subject owner;
    private String runID;
    private List<Result> resultsList;
    private List<Parameter> parameterList;
    private String requestPath;
    private String requesterIp;
    private JobInfo jobInfo;


    public Job() { }

    /**
     * Copy constructor. Create a new job with the specified ID and content copied
     * from the specified job.
     * 
     * @param jobID
     * @param job
     */
    public Job(String jobID, Job job)
    {
        this.jobID = jobID;
        setAll(job);
    }

    /**
     * Constructor.
     *
     * @param jobID                 The unique Job ID.
     * @param executionPhase        The Execution Phase.
     * @param executionDuration     The Duration in clock seconds.
     * @param destructionTime       The date and time of destruction.
     * @param quote                 The quoted date of completion.
     * @param startTime             The start date of execution.
     * @param endTime               The end date of execution.
     * @param errorSummary          The error, if any.
     * @param owner                 The Owner of this Job.
     * @param runID                 The specific running ID.
     * @param resultsList           The List of Results.
     * @param parameterList         The List of Parameters.
     * @param requestPath           The HTTP request path.
     * @param requesterIp           The HTTP requester's IP address
     */
    public Job(final String jobID, final ExecutionPhase executionPhase,
               final long executionDuration, final Date destructionTime,
               final Date quote, final Date startTime, final Date endTime,
               final ErrorSummary errorSummary, final Subject owner,
               final String runID, final List<Result> resultsList,
               final List<Parameter> parameterList,
               final String requestPath, final String requesterIp)
    {
        this.jobID = jobID;
        this.executionPhase = executionPhase;
        this.executionDuration = executionDuration;
        this.destructionTime = destructionTime;
        this.quote = quote;
        this.startTime = startTime;
        this.endTime = endTime;
        this.errorSummary = errorSummary;
        this.owner = owner;
        this.runID = runID;
        this.resultsList = resultsList;
        this.parameterList = parameterList;
        this.requestPath = requestPath;
        this.requesterIp = requesterIp;
        this.jobInfo = null;
    }

    /**
     * Constructor.
     *
     * @param jobID                 The unique Job ID.
     * @param executionPhase        The Execution Phase.
     * @param executionDuration     The Duration in clock seconds.
     * @param destructionTime       The date and time of destruction.
     * @param quote                 The quoted date of completion.
     * @param startTime             The start date of execution.
     * @param endTime               The end date of execution.
     * @param errorSummary          The error, if any.
     * @param owner                 The Owner of this Job.
     * @param runID                 The specific running ID.
     * @param resultsList           The List of Results.
     * @param requestPath           The HTTP request path.
     * @param requesterIp           The HTTP requester's IP address
     * @param jobInfo               Content that describes the Job.
     */
    public Job(final String jobID, final ExecutionPhase executionPhase,
               final long executionDuration, final Date destructionTime,
               final Date quote, final Date startTime, final Date endTime,
               final ErrorSummary errorSummary, final Subject owner,
               final String runID, final List<Result> resultsList,
               final JobInfo jobInfo, final String requestPath, final String requesterIp)
    {
        this.jobID = jobID;
        this.executionPhase = executionPhase;
        this.executionDuration = executionDuration;
        this.destructionTime = destructionTime;
        this.quote = quote;
        this.startTime = startTime;
        this.endTime = endTime;
        this.errorSummary = errorSummary;
        this.owner = owner;
        this.runID = runID;
        this.resultsList = resultsList;
        this.jobInfo = jobInfo;
        this.requestPath = requestPath;
        this.requesterIp = requesterIp;
        this.parameterList = null;
    }
    /**
     * Set all fields except ID from the specified job.
     * 
     * @param job
     */
    public void setAll(Job job)
    {
        setExecutionPhase(job.getExecutionPhase());
        setExecutionDuration(job.getExecutionDuration());
        setDestructionTime(job.getDestructionTime());
        setQuote(job.getQuote());
        setStartTime(job.getStartTime());
        setEndTime(job.getEndTime());
        setErrorSummary(job.getErrorSummary());
        setOwner(job.getOwner());
        setRunID(job.getRunID());
        setResultsList(job.getResultsList());
        setParameterList(job.getParameterList());
        setOwner(job.getOwner());
        setRequestPath(job.getRequestPath());
        setRequesterIp(job.getRequesterIp());
        setJobInfo(job.getJobInfo());
    }

    @Override
    public String toString()
    {
        return "Job [jobInfo=" + jobInfo + " destructionTime=" + destructionTime + ", endTime=" + endTime + ", errorSummary="
                + errorSummary + ", executionDuration=" + executionDuration + ", executionPhase=" + executionPhase + ", jobID="
                + jobID + ", owner=" + owner + ", parameterList=" + parameterList + ", quote=" + quote + ", requestPath="
                + requestPath + ", requesterIp="
                + requesterIp + ", resultsList=" + resultsList + ", runID=" + runID + ", startTime=" + startTime + "]";
    }

    public void setID(String jobID)
    {
        this.jobID = jobID;
    }

    /**
     * Obtain the unique Job ID.
     *
     * @return Long job ID.
     */
    public String getID()
    {
        return jobID;
    }

    /**
     * Obtain the Execution Phase.
     *
     * @return ExecutionPhase instance.
     */
    public ExecutionPhase getExecutionPhase()
    {
        return executionPhase;
    }

    /**
     * Set the new Phase.
     *
     * @param executionPhase The new Phase.
     */
    public void setExecutionPhase(final ExecutionPhase executionPhase)
    {
        this.executionPhase = executionPhase;
    }

    /**
     * An Execution Duration object defines the duration for which a job shall
     * run.  This represents the "computation time" that a job is to be
     * allowed, although because a specific measure of CPU time may not be
     * available in all environments, this duration is defined in real clock
     * seconds. An execution duration of 0 implies unlimited execution
     * duration.
     * <p/>
     * When the execution duration has been exceeded the service will
     * automatically abort the job, which has the same effect as when a manual
     * "Abort" is requested.
     * <p/>
     * When a job is created, the service sets the initial execution duration.
     * The client may write to an Execution Duration to try to change the
     * job's CPU time allocation.  The service may forbid changes, or may set
     * limits on the allowed execution duration.
     *
     * @return long execution duration.
     */
    public long getExecutionDuration()
    {
        return executionDuration;
    }

    /**
     * Set the new value, in seconds.
     *
     * @param executionDuration New execution duration value, in seconds.
     */
    public void setExecutionDuration(final long executionDuration)
    {
        this.executionDuration = executionDuration;
    }

    /**
     * The Destruction Time object represents the instant when the job shall be
     * destroyed. The Destruction Time is an absolute time.
     * <p/>
     * The Destruction Time may be viewed as a measure of the amount of time
     * that a service is prepared to allocate storage for a job – typically
     * this will be a longer duration that the amount of CPU time that a
     * service would allocate.
     * <p/>
     * When a job is created the service sets the initial Destruction Time.
     * The client may write to the Destruction Time to try to change the life
     * expectancy of the job. The service may forbid changes, or may set limits
     * on the allowed destruction time.
     *
     * @return Date of destruction.
     */
    public Date getDestructionTime()
    {
        return destructionTime;
    }

    /**
     * Set the new date of Destruction.
     *
     * @param destructionTime Date of destruction.
     */
    public void setDestructionTime(final Date destructionTime)
    {
        this.destructionTime = destructionTime;
    }

    /**
     * A Quote object predicts when the job is likely to complete. The
     * intention is that a client creates the same job on several services,
     * compares the quotes and then accepts the best quote.
     * <p/>
     * Quoting for a computational job is notoriously difficult. A UWS
     * implementation must always provide a quote object, in order that the
     * two-phase committal of jobs be uniform across all UWS, but it may supply
     * a "don't know" answer for the completion time.
     *
     * @return Date Quote.
     */
    public Date getQuote()
    {
        return quote;
    }

    public void setQuote(final Date quote)
    {
        this.quote = quote;
    }

    public void setStartTime(final Date startTime)
    {
        this.startTime = startTime;
    }

    public void setEndTime(final Date endTime)
    {
        this.endTime = endTime;
    }

    public void setOwner(final Subject owner)
    {
        this.owner = owner;
    }

    public void setRunID(final String runID)
    {
        this.runID = runID;
    }

    /**
     * Obtain the instant at which the job started execution.
     *
     * @return Date of start.
     */
    public Date getStartTime()
    {
        return startTime;
    }

    /**
     * Obtain the instant at which the job completed execution.
     *
     * @return Date at end of execution.
     */
    public Date getEndTime()
    {
        return endTime;
    }

    /**
     * The error object gives a human readable error message (if any) for the
     * underlying job. This object is intended to be a detailed error message, and
     * consequently might be a large piece of text such as a stack trace. When
     * there is an error running a job a summary of the error should be given using
     * the optional errorSummary element of the JobSummary type.
     *
     * @return String human readable message.
     */
    public ErrorSummary getErrorSummary()
    {
        return errorSummary;
    }

    public void setErrorSummary(final ErrorSummary errorSummary)
    {
        this.errorSummary = errorSummary;
    }

    /**
     * The owner object represents the identifier for the creator of the job.
     * This object will not exist for all invocations of a UWS conformant
     * protocol, but only in cases where the access to the service is
     * authenticated.
     *
     * @return String Owner Name.
     */
    public Subject getOwner()
    {
        return owner;
    }

    /**
     * The RunId object represents an identifier that the job creator uses to
     * identify the job. Note that this is distinct from the Job Identifier
     * that the UWS system itself assigns to each job. The UWS system should do
     * no parsing or processing of the RunId, but merely pass back the value
     * (if it exists) as it was passed to the UWS at job creation time. In
     * particular it may be the case that multiple jobs have the same RunId, as
     * this is a mechanism by which the calling process can identify jobs that
     * belong to a particular group. The exact mechanism of setting the RunId
     * is not specified here, but will be part of the specification of the
     * protocol using the UWS pattern.
     *
     * @return String run ID.
     */
    public String getRunID()
    {
        return runID;
    }

    /**
     * The Results List object is a container for formal results of the job.
     * Its children may be any objects resulting from the computation that may
     * be fetched from the service when the job has completed.
     * <p/>
     * Reading the Results List itself enumerates the available or expected
     * result objects.
     * <p/>
     * The children of the Results List may be read but not updated or deleted.
     * The client may not add anything to the Results List.
     *
     * @return ResultList instance.
     */
    public List<Result> getResultsList()
    {
        if (resultsList == null)
        {
            setResultsList(new ArrayList<Result>());
        }

        return Collections.unmodifiableList(resultsList);
    }

    /**
     * Set the results of the underlying List of results.
     *
     * @param resultList List of Result instances, never null.
     */
    public void setResultsList(final List<Result> resultList)
    {
        this.resultsList = resultList;
    }

    /**
     * Reading the Results List itself enumerates the available or expected
     * result objects.
     * <p/>
     * A particular implementation of UWS may choose to allow the parameters to
     * be updated after the initial job creation step, before the Phase is set
     * to the executing state. It is up to the individual implementation to
     * specify exactly how these parameters may be updated, but good practice
     * would be to choose one of the following options.
     * <p/>
     * 1.   HTTP POST an application/x-www-form-urlencoded parameter name,
     * value pair to either
     * 1.  /{jobs}/{job-id)
     * 2.  /{jobs}/{job-id)/parameters
     * <p/>
     * 2.   HTTP PUT the parameter value to
     * /{jobs}/{job-id)/parameters/(parameter-name)
     *
     * @return ParameterList instance.
     */
    public List<Parameter> getParameterList()
    {
        if (parameterList == null)
        {
            setParameterList(new ArrayList<Parameter>());
        }

        return parameterList;
    }

    public void setParameterList(final List<Parameter> parameterList)
    {
        this.parameterList = parameterList;
    }

    /**
     * Add a parameter to the Parameter List.
     *
     * @param parameter  A Parameter to add.  NULLs are not allowed, and
     *                   duplicates will be discarded.
     */
    public void addParameter(final Parameter parameter)
    {
        if (!getParameterList().contains(parameter))
        {
            getParameterList().add(parameter);
        }
    }

    /**
     * Path of the Request that created the Job.
     * 
     * @return The Request Path.
     */
    public String getRequestPath()
    {
        return requestPath;
    }

    /**
     * Path of the Request that created the Job.
     *
     * @param path The Request Path.
     */
    public void setRequestPath(final String path)
    {
        this.requestPath = path;
    }

    /**
     * The nebulous ANY object accommodates a domain object meaningful to
     * specific implementors of the UWS system.  The default implementation will
     * not make use of it at all.
     *
     * @return  An Object.  Anything in the world.
     */
    public JobInfo getJobInfo()
    {
        return jobInfo;
    }

    public void setJobInfo(final JobInfo jobInfo)
    {
        this.jobInfo = jobInfo;
    }

    /**
     * @param requesterIp the requesterIp to set
     */
    public void setRequesterIp(String requesterIp)
    {
        this.requesterIp = requesterIp;
    }

    /**
     * @return the requesterIp
     */
    public String getRequesterIp()
    {
        return requesterIp;
    }
}
