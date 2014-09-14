/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.titan;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import co.indexia.antiquity.graph.ActiveVersionedGraph;
import co.indexia.antiquity.graph.Configuration;
import co.indexia.antiquity.graph.TransactionalVersionedGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Graph;
import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.antiquity.AntiquityService;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * Titan Graph DB Service.
 */
@XmlRootElement(name = "titan")
public class Titan extends AntiquityService {
    /**
     * The path where DB files are stored.
     */
    @XmlElement
    @XmlDefaultValue("${dataHome}/titan")
    private String path;

    /***
     * The backend type of the store
     */
    @XmlElement
    @XmlDefaultValue("BERKELEY")
    Backend backend;

    @XmlElement
    public SchemaConfig schema;
    // XmLElementWrapper generates a wrapper element around XML representation
    @XmlElementWrapper(name = "indices")
    // XmlElement sets the name of the entities
    @XmlElement(name = "index")
    List<IndexConfig> indices;

    private Configuration antiquityConfig;

    private TitanFactory.Builder titanBuilder;

    private class BlueprintsGraphProvider implements Provider<Graph> {
        @Override
        public Graph get() {
            return titanBuilder.open();
        }
    }

    private class VersionedGraphProvider implements Provider<TransactionalVersionedGraph<TitanGraph, Long>> {
        @Inject
        Graph graphDb;

        @Override
        public TransactionalVersionedGraph<TitanGraph, Long> get() {
            return (TransactionalVersionedGraph) new ActiveVersionedGraph.ActiveVersionedTransactionalGraphBuilder<TitanGraph, Long>(
                    (TitanGraph) graphDb, new LongGraphIdentifierBehavior()).init(init).conf(antiquityConfig).build();
        }
    }

    @Override
    public Module configure() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                if (backend==null) {
                    throw new IllegalArgumentException("The given backend type is not supported.");
                }

                antiquityConfig = new Configuration.ConfBuilder().privateVertexHashEnabled(privateVertexHash)
                        .useNaturalIds(naturalIds)
                        .useNaturalIdsOnlyIfSuppliedIdsAreIgnored(naturalIdsOnlyIfSuppliedIdsAreIgnored)
                        .doNotVersionEmptyTransactions(dontVersionEmptyTransactions).build();

                titanBuilder = TitanFactory.build().set("storage.backend", backend.getBackendType()).set("storage.directory", path);

                //configure schema
                if (schema != null && schema.getSchemaDefault() != null) {
                    titanBuilder.set("schema.default", schema.getSchemaDefault());
                }

                if (indices != null){
                    for (IndexConfig indexConfig : indices){
                        if (indexConfig.backend == IndexBackend.lucene){
                            //set index backend
                            titanBuilder.set(String.format("index.%s.backend",indexConfig.name), IndexBackend.lucene.getBackendType());
                            titanBuilder.set(String.format("index.%s.index-name",indexConfig.name), indexConfig.name);
                        }
                        if (!Strings.isNullOrEmpty(indexConfig.path)){
                            titanBuilder.set(String.format("index.%s.directory", indexConfig.name), indexConfig.path);
                        }
                    }
                }

                bind(Graph.class).toProvider(new BlueprintsGraphProvider()).in(Scopes.SINGLETON);
                bind(new TypeLiteral<TransactionalVersionedGraph<TitanGraph, Long>>() {
                }).toProvider(
                        new VersionedGraphProvider()).in(Scopes.SINGLETON);
            }
        };
    }

    public enum Backend {
        memory("inmemory"), berkeley("berkeleyje");

        Backend(String backendType) {
            this.backendType = backendType;
        }

        private String backendType;

        public String getBackendType() {
            return backendType;
        }
    }

    public enum IndexBackend{
        lucene("lucene");

        IndexBackend(String backendType) {
            this.backendType = backendType;
        }

        private String backendType;

        public String getBackendType() {
            return backendType;
        }
    }


    public static class IndexConfig{
        @XmlElement
        String name;

        @XmlElement
        IndexBackend backend;

        @XmlElement
        @XmlDefaultValue("${dataHome}/titan_search")
        String path;
    }
}