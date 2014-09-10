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
package org.atteo.moonshine.titan;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import co.indexia.antiquity.graph.TransactionalVersionedGraph;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

@MoonshineConfiguration(fromString = ""
        + "<config>"
        + "     <titan>"
        + "         <path>${dataHome}/titan</path>"
        + "         <backend>memory</backend>     "
        + "         <init>false</init>             "
        + "         <schema><default>none</default></schema>"
        + "     </titan>"
        + "</config>")
public class TitanTest extends MoonshineTest {
    @Inject
    Graph db1;

    @Inject
    TransactionalVersionedGraph<TitanGraph, Long> versionedGraph;

    @Test
    public void trivial() {
        List<Vertex> db1Vertices = Lists.newArrayList(db1.getVertices());
        int verticesSize = db1Vertices.size();
        Vertex db1Vertex = db1.addVertex("vv");
        db1Vertices = Lists.newArrayList(db1.getVertices());
        assertThat(db1Vertices, hasItem(db1Vertex));
        assertThat(db1Vertices.size(), is(verticesSize + 1));
    }

    @Test
    public void antiquityTest() {
        assertThat(versionedGraph, notNullValue());
        Vertex foo = versionedGraph.addVertex("foo-v");
        foo.setProperty("key", "foo!");
        versionedGraph.commit();
        long revision1 = versionedGraph.getLatestGraphVersion();
        Vertex loadedFoo = versionedGraph.getVertex("foo-v");
        assertThat(loadedFoo.getId(), is(foo.getId()));
        assertThat(loadedFoo.getProperty("key").toString(), is("foo!"));
        loadedFoo.setProperty("key", "bar!");
        versionedGraph.commit();
        long revision2 = versionedGraph.getLatestGraphVersion();
        loadedFoo = versionedGraph.getVertex("foo-v");
        assertThat(loadedFoo.getProperty("key").toString(), is("bar!"));
        assertThat(versionedGraph.getHistoricGraph().getVertexForVersion(loadedFoo.getId(), revision1).getProperty("key").toString(), is("foo!"));
        assertThat(versionedGraph.getHistoricGraph().getVertexForVersion(loadedFoo.getId(), revision2).getProperty("key").toString(), is("bar!"));
    }
}
