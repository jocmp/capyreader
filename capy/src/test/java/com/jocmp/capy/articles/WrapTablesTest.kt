package com.jocmp.capy.articles

import com.jocmp.capy.articles.HtmlHelpers.html
import kotlin.test.Test

class WrapTablesTest {
    @Test
    fun `wraps tables`() {
        val table =
            """
            <table>
              <tr>
                <th scope="col">Person</th>
                <th scope="col">Most interest in</th>
                <th scope="col">Age</th>
              </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">Chris</th>
                  <td>HTML tables</td>
                  <td>22</td>
                </tr>  
                <tr>
                  <th scope="row">Sarah</th>
                  <td>JavaScript frameworks</td>
                  <td>29</td>
                </tr>
                <tr>
                  <th scope="row">Karen</th>
                  <td>Web performance</td>
                  <td>36</td>
                </tr>
              </tbody>
            </table>
            """.trimIndent()

        val document = html(table)

        wrapTables(document)

        HtmlHelpers.assertEquals(document) {
            """
            <div class="table__wrapper">
             <table>
              <tbody>
               <tr>
                <th scope="col">Person</th>
                <th scope="col">Most interest in</th>
                <th scope="col">Age</th>
               </tr>
              </tbody>
              <tbody>
               <tr>
                <th scope="row">Chris</th>
                <td>HTML tables</td>
                <td>22</td>
               </tr>
               <tr>
                <th scope="row">Sarah</th>
                <td>JavaScript frameworks</td>
                <td>29</td>
               </tr>
               <tr>
                <th scope="row">Karen</th>
                <td>Web performance</td>
                <td>36</td>
               </tr>
              </tbody>
             </table>
            </div>
            """
        }
    }
}
