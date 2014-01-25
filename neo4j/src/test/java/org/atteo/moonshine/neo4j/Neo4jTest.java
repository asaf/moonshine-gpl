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
package org.atteo.moonshine.neo4j;

import org.atteo.moonshine.blueprints.BlueprintsTest;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "     <neo4j id=\"default\">"
		+ "         <type>impermanent</type>"
		+ "     </neo4j>"
		+ "     <neo4j id=\"db2\">"
		+ "         <type>impermanent</type>"
		+ "     </neo4j>"
		+ "</config>")
public class Neo4jTest extends BlueprintsTest {
	@Inject
	@Named("default")
	GraphDatabaseService db1;

	@Inject
	@Named("db2")
	GraphDatabaseService db2;

	@Test
	public void trivial() {
		int db1NodesAmount = Lists.newArrayList(db1.getAllNodes()).size();
		int db2NodesAmount = Lists.newArrayList(db1.getAllNodes()).size();

		assertThat(db1, notNullValue());

		Transaction tx = db1.beginTx();
		Node n1 = null;
		try {
			n1 = db1.createNode();
			n1.setProperty("foo", "bar");
			tx.success();
		} finally {
			tx.finish();
		}

		assertThat(db1.getNodeById(n1.getId()), is(n1));

		try {
			assertThat(db2.getNodeById(n1.getId()), nullValue());
			assertTrue(false);
		} catch (NotFoundException e) {
		}

		assertThat(db1, not(db2));
		assertThat(db1, not(db2));
		assertThat(db1NodesAmount + 1, is(Lists.newArrayList(db1.getAllNodes()).size()));
		assertThat(db2NodesAmount, is(Lists.newArrayList(db2.getAllNodes()).size()));
	}
}
