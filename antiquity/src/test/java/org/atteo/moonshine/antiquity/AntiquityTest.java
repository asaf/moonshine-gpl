/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.antiquity;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import co.indexia.antiquity.graph.TransactionalVersionedGraph;
import org.atteo.moonshine.blueprints.BlueprintsTest;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;


@MoonshineConfiguration(fromString = ""
        + "<config>"
        + "</config>")
public abstract class AntiquityTest<G extends TransactionalGraph & IndexableGraph & KeyIndexableGraph, T extends Comparable<T>> extends BlueprintsTest {
    Class<G> graph;
    Class<T> type;
    TransactionalVersionedGraph<G, T> vdb;

    public AntiquityTest(Class<G> graph, Class<T> type) {
        this.graph = graph;
        this.type = type;
    }


    @Inject
    Injector injector;

    @Before
    public void setup() {
        Type t = Types.newParameterizedType(TransactionalVersionedGraph.class, graph, type);
        this.vdb = (TransactionalVersionedGraph<G, T>) injector.getInstance(Key.get(t, Names.named("default")));
    }

    @Test
    public void shouldInjectGraph() {
        assertThat(vdb).isNotNull();
    }

    @Test
    public void shouldPerformBasicOperationsOnVersionedGraph() throws SQLException {
        Vertex v1 = vdb.addVertex(null);
        v1.setProperty("foo", "foo");
        vdb.commit();
        T ver1 = vdb.getLatestGraphVersion();
        v1.setProperty("foo", "bar");
        vdb.commit();
        T ver2 = vdb.getLatestGraphVersion();
        assertThat(vdb.getHistoricGraph().getVertexForVersion(v1.getId(), ver1).getProperty("foo")).isEqualTo("foo");
        assertThat(vdb.getHistoricGraph().getVertexForVersion(v1.getId(), ver2).getProperty("foo")).isEqualTo("bar");
    }
}
