/*
 *   Copyright 2007 panFMP Developers Team c/o Uwe Schindler
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package de.pangaea.metadataportal.search;

import java.rmi.RemoteException;
import org.apache.axis.ConfigurationException;
import org.apache.axis.transport.http.HTTPConstants;
import javax.servlet.http.HttpServlet;

public class SearchServiceAxisImpl {
    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(SearchServiceAxisImpl.class);

    private SearchService searchService;

    // default constructor that fetches config and other things from servlet configuration
    public SearchServiceAxisImpl() throws ConfigurationException {
        org.apache.axis.MessageContext mcontext=org.apache.axis.MessageContext.getCurrentContext();
        if (mcontext==null)
            throw new ConfigurationException("The constructor "+getClass().getName()+"() only works inside an Apache Axis environment!");
        java.util.Map axisCfg=mcontext.getAxisEngine().getConfig().getGlobalOptions();

        String cfgFile=(String)axisCfg.get("de.pangaea.metadataportal.search.indexConfigFile");
        if (cfgFile==null)
            throw new ConfigurationException("The configuration file for the indices must be given as global configuration parameter 'de.pangaea.metadataportal.search.indexConfigFile' in the Apache AXIS configuration (WEB-INF/server-config.wsdd)!");
        HttpServlet servlet = (HttpServlet)mcontext.getProperty(HTTPConstants.MC_HTTP_SERVLET);
        if (servlet==null)
            throw new ConfigurationException("Cannot get Axis servlet instance from MessageContext!");
        String webinf=servlet.getServletContext().getRealPath("/WEB-INF/");
        if (webinf==null)
            throw new ConfigurationException("Cannot resolve WEB-INF directory!");

        try {
            cfgFile=(new java.io.File(webinf,cfgFile)).getCanonicalPath();
            searchService=new SearchService(cfgFile);
        } catch (Exception e) {
            throw new ConfigurationException("Cannot initialize search service with '"+cfgFile+"' as index configuration file: "+e);
        }
    }

    // test method
    public String echo(String echo) {
        return echo;
    }

    // wrappers for axis around the other methods
    public SearchResponse search(SearchRequest searchRequest, int offset, int count) throws RemoteException {
        try {
            return searchService.search(searchRequest,offset,count);
        } catch (Exception e) {
            log.error("Error during Lucene query",e);
            throw new RemoteException("Error during Lucene query",e);
        }
    }

    public SearchResponseItem getDocument(String index, String identifier)  throws RemoteException {
        try {
            return searchService.getDocument(index,identifier);
        } catch (Exception e) {
            log.error("Error during Lucene query",e);
            throw new RemoteException("Error during Lucene query",e);
        }
    }

    public String[] suggest(String indexName, SearchRequestQuery query, int count) throws RemoteException {
        try {
            return searchService.suggest(indexName,query,count);
        } catch (Exception e) {
            log.error("Error during Lucene query",e);
            throw new RemoteException("Error during Lucene query",e);
        }
    }

    public String[] listTerms(String indexName, String fieldName, int count) throws RemoteException {
        try {
            return searchService.listTerms(indexName,fieldName,count);
        } catch (Exception e) {
            log.error("Error during Lucene query",e);
            throw new RemoteException("Error during Lucene query",e);
        }
    }

}