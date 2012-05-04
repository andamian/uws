/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2011.                            (c) 2011.
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
*  $Revision: 5 $
*
************************************************************************
*/

package ca.nrc.cadc.uws.server;

import ca.nrc.cadc.uws.Job;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * JobExecutor implementation that uses a pool of threads for execution. The
 * pool limits the number of jobs that run simultaneously, which may be beneficial
 * if resources are limited or contention would make aggregate execution times worse.
 * 
 * @author pdowler
 */
public class ThreadPoolExecutor extends AbstractExecutor
{
    private static final Logger log = Logger.getLogger(ThreadPoolExecutor.class);

    private final static int INITIAL_QUEUE_SIZE = 21;
    
    private final Map<String,CurrentJob> currentJobs = new HashMap<String,CurrentJob>();
    
    private java.util.concurrent.ThreadPoolExecutor threadPool;
    private String poolName;

    /**
     * Constructor. Uses a default priority comparator (FIFO) and default poolName.
     *
     * @param jobUpdater JobUpdater implementation
     * @param jobRunnerClass JobRunner implementation class
     * @param poolSize minimum (initial) number of running threads
     */
    public ThreadPoolExecutor(JobUpdater jobUpdater, Class jobRunnerClass, int poolSize)
    {
        super(jobUpdater, jobRunnerClass);
        init(poolSize, ThreadPoolExecutor.class.getSimpleName(), new DefaultPriorityComparator());
    }

    /**
     * Constructor. Uses a default priority comparator (FIFO).
     *
     * @param jobUpdater JobUpdater implementation
     * @param jobRunnerClass JobRunner implementation class
     * @param poolName name of this pool (base name for threads)
     * @param poolSize minimum (initial) number of running threads
     */
    public ThreadPoolExecutor(JobUpdater jobUpdater, Class jobRunnerClass,
            int poolSize, String poolName)
    {
        super(jobUpdater, jobRunnerClass);
        init(poolSize, poolName, new DefaultPriorityComparator());
    }

    /**
     * Constructor.
     *
     * @param jobUpdater JobUpdater implementation
     * @param jobRunnerClass JobRunner implementation class
     * @param poolName name of this pool (for naming threads)
     * @param poolSize minimum (initial) number of running threads
     * @param priorityComparator comparator to order queued jobs
     */
    public ThreadPoolExecutor(JobUpdater jobUpdater, Class jobRunnerClass,
            int poolSize, String poolName, Comparator<CurrentJob> priorityComparator)
    {
        super(jobUpdater, jobRunnerClass);
        init(poolSize, poolName, priorityComparator);
    }

    private void init(int poolSize, String poolName, Comparator<CurrentJob> priorityComparator)
    {
        
        if (poolName == null)
            throw new IllegalArgumentException("poolName cannot be null");
        if (poolSize < 1)
            throw new IllegalArgumentException("poolSize must be > 0");
        if (priorityComparator == null)
            throw new IllegalArgumentException("priorityComparator cannot be null");
        this.poolName = poolName + "-";

        Comparator<Runnable> cr = new WrapperPriorityComparator(priorityComparator);
        // note: PriorityBlockingQueue is unbounded so maxPoolSize is ignored (=poolSize)
        // note: core threads are kept alive unless we want to also enable allowCoreThreadTimeOut
        this.threadPool =
            new java.util.concurrent.ThreadPoolExecutor(poolSize, poolSize,
                Long.MAX_VALUE, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(INITIAL_QUEUE_SIZE, cr));
        threadPool.setThreadFactory(new DaemonThreadFactory());
        //threadPool.allowCoreThreadTimeOut(true);
    }

    private class DaemonThreadFactory implements ThreadFactory
    {
        private int num = 1;
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r);
            t.setName(poolName + Integer.toString(num++));
            log.debug("created: " + t.getName());
            t.setDaemon(true); // so the thread will not block application shutdown
            return t;
        }
    }

    /**
     * Execute async job. This method hands the job off to a thread pool for
     * execution.
     *
     * @param job
     * @param jobRunner
     */
    @Override
    protected final void executeAsync(Job job, JobRunner jobRunner)
    {
        CurrentJob cj = new CurrentJob(job, jobRunner, System.currentTimeMillis());
        synchronized(currentJobs)
        {
            this.currentJobs.put(job.getID(), cj);
        }
        cj.future = this.threadPool.submit(cj);
    }

    /**
     * Abort tyhe job. This method removes the executing job from the thread pool
     * list of queued jobs. If the job already started executing, this method does
     * nothing.
     *
     * @param jobID
     */
    @Override
    protected final void abortJob(String jobID)
    {
        synchronized(currentJobs)
        {
            CurrentJob r = currentJobs.remove(jobID);
            if (r != null)
            {
                if (r.future != null)
                    r.future.cancel(true);     // try to interrupt running
                threadPool.remove(r.runnable); // try to remove queued
            }
        }
    }

    /**
     * Simple wrapper to contain the job, jobRunner, and time.
     */
    public final class CurrentJob implements Runnable
    {
        public Job job;
        public JobRunner runnable;
        public Long queuedTime;
        private Future future;
        
        CurrentJob(Job job, JobRunner runnable, long queuedTime)
        {
            this.job = job;
            this.runnable = runnable;
            this.queuedTime = new Long(queuedTime);
        }

        @Override
        public int hashCode() { return job.getID().hashCode(); }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof CurrentJob)
            {
                CurrentJob cj = (CurrentJob) o;
                return job.getID().equals(cj.job.getID());
            }
            return false;
        }

        public void run()
        {
            runnable.run();
            synchronized(currentJobs)
            {
                currentJobs.remove(job.getID());
            }
        }

    }

    private class WrapperPriorityComparator implements Comparator<Runnable>
    {
        private Comparator<CurrentJob> cmp;

        public WrapperPriorityComparator( Comparator<CurrentJob> cmp)
        {
            this.cmp = cmp;
        }

        public int compare(Runnable o1, Runnable o2)
        {
            CurrentJob c1 = (CurrentJob) o1;
            CurrentJob c2 = (CurrentJob) o2;
            return cmp.compare(c1, c2);
        }
    }

    private class DefaultPriorityComparator implements Comparator<CurrentJob>
    {
        public int compare(CurrentJob o1, CurrentJob o2)
        {
            return o1.queuedTime.compareTo(o2.queuedTime);
        }
    }
}
