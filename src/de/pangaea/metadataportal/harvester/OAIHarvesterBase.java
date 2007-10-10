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

package de.pangaea.metadataportal.harvester;

import de.pangaea.metadataportal.utils.*;
import de.pangaea.metadataportal.config.*;
import java.util.*;
import java.net.URL;
import java.io.*;

public abstract class OAIHarvesterBase extends Harvester {
	// Class members
	public static final String OAI_NS="http://www.openarchives.org/OAI/2.0/";
	public static final String OAI_STATICREPOSITORY_NS="http://www.openarchives.org/OAI/2.0/static-repository";

	public static final int DEFAULT_RETRY_TIME = 60; // seconds
	public static final int DEFAULT_RETRY_COUNT = 5;

	// Object members
	protected String metadataPrefix=null;
	protected Set<String> sets=null;
	protected int retryCount=DEFAULT_RETRY_COUNT;
	protected int retryTime=DEFAULT_RETRY_TIME;

	// construtor
	@Override
	public void open(SingleIndexConfig iconfig) throws Exception {
		super.open(iconfig);

		String setSpec=iconfig.harvesterProperties.getProperty("setSpec");
		if (setSpec!=null) {
			sets=new HashSet<String>();
			Collections.addAll(sets,setSpec.split("[\\,\\;\\s]+"));
			if (sets.size()==0) sets=null;
		}

		String retryCountStr=iconfig.harvesterProperties.getProperty("retryCount");
		if (retryCountStr!=null) retryCount=Integer.parseInt(retryCountStr);
		String retryTimeStr=iconfig.harvesterProperties.getProperty("retryAfterSeconds");
		if (retryTimeStr!=null) retryTime=Integer.parseInt(retryTimeStr);
		metadataPrefix=iconfig.harvesterProperties.getProperty("metadataPrefix");
		if (metadataPrefix==null) throw new NullPointerException("No metadataPrefix for the OAI repository was given!");
	}

	@Override
	public void addDocument(MetadataDocument mdoc) throws IndexBuilderBackgroundFailure,InterruptedException {
		if (sets!=null) {
			if (Collections.disjoint(((OAIMetadataDocument)mdoc).getSets(),sets)) mdoc.setDeleted(true);
		}
		super.addDocument(mdoc);
	}


	// harvester code

	protected void doParse(ExtendedDigester dig, String url, int retryCount) throws Exception {
		URL u=new URL(url);
		try {
			dig.clear();
			dig.resetRoot();
			dig.push(this);
			dig.parse(OAIDownload.getInputSource(u));
		} catch (org.xml.sax.SAXException saxe) {
			// throw the real Exception not the digester one
			if (saxe.getException()!=null) throw saxe.getException();
			else throw saxe;
		} catch (IOException ioe) {
			int after=retryTime;
			if (ioe instanceof RetryAfterIOException) {
				if (retryCount==0) throw (IOException)ioe.getCause();
				log.warn("OAI server returned '503 Service Unavailable' with a 'Retry-After' value being set.");
				after=((RetryAfterIOException)ioe).getRetryAfter();
			} else {
				if (retryCount==0) throw ioe;
				log.error("OAI server access failed with exception: ",ioe);
			}
			log.info("Retrying after "+after+" seconds ("+retryCount+" retries left)...");
			try { Thread.sleep(1000L*after); } catch (InterruptedException ie) {}
			doParse(dig,url,retryCount-1);
		}
	}

	protected void reset() {
	}

	@Override
	public void close() throws Exception {
		reset();
		super.close();
	}
	
	@Override
	public List<String> getValidHarvesterPropertyNames() {
		ArrayList<String> l=new ArrayList<String>(super.getValidHarvesterPropertyNames());
		l.addAll(Arrays.<String>asList(
			"setSpec",
			"retryCount",
			"retryAfterSeconds",
			"metadataPrefix"
		));
		return l;
	}

}