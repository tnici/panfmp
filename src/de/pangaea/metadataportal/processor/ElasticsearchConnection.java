/*
 *   Copyright panFMP Developers Team c/o Uwe Schindler
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

package de.pangaea.metadataportal.processor;

import java.io.Closeable;
import java.io.IOException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;

import de.pangaea.metadataportal.config.Config;
import de.pangaea.metadataportal.config.HarvesterConfig;

/**
 * TODO
 * 
 * @author Uwe Schindler
 */
public class ElasticsearchConnection implements Closeable {
  private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ElasticsearchConnection.class);
  
  private Client client;

  public static final int ELASTICSEARCH_DEFAULT_PORT = 9300;

  public ElasticsearchConnection(Config config) {
    final Settings settings = config.esSettings == null ? ImmutableSettings.Builder.EMPTY_SETTINGS : config.esSettings;
    log.info("Connecting to Elasticsearch nodes: " + config.esTransports);
    if (log.isDebugEnabled()) {
      log.debug("ES connection settings: " + settings.getAsMap());
    }
    this.client = new TransportClient(settings, false)
      .addTransportAddresses(config.esTransports.toArray(new TransportAddress[config.esTransports.size()]));
  }

  @Override
  public void close() {
    client.close();
    client = null;
    log.info("Closed connection to Elasticsearch.");
  }
  
  public Client client() {
    if (client == null)
      throw new IllegalStateException("Elasticsearch TransportClient is already closed.");
    return client;
  }
  
  public DocumentProcessor getDocumentProcessor(HarvesterConfig iconfig, String targetIndex) throws IOException {
    return new DocumentProcessor(client(), iconfig, targetIndex);
  }
  
}