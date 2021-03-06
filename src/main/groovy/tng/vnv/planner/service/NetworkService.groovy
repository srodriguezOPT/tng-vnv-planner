/*
 * Copyright (c) 2015 SONATA-NFV, 2019 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * ALL RIGHTS RESERVED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * This work has been performed in the framework of the SONATA project,
 * funded by the European Commission under Grant number 671517 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the SONATA
 * partner consortium (www.sonata-nfv.eu).
 *
 * This work has been performed in the framework of the 5GTANGO project,
 * funded by the European Commission under Grant number 761493 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the 5GTANGO
 * partner consortium (www.5gtango.eu).
 */

package tng.vnv.planner.service

import tng.vnv.planner.utils.TangoLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tng.vnv.planner.client.Gatekeeper
import tng.vnv.planner.repository.TestSetRepository

@Service
class NetworkService {

    @Autowired
    Gatekeeper gatekeeper

    @Autowired
    TestSetRepository testSetRepository

    //Tango logger
    def tangoLogger = new TangoLogger()
    String tangoLoggerType = null;
    String tangoLoggerOperation = null;
    String tangoLoggerMessage = null;
    String tangoLoggerStatus = null;

    List findTestsByService(def serviceUuid){
        tangoLoggerType = "I";
        tangoLoggerOperation = "NetworkService.findTestsByService";
        tangoLoggerMessage = ("Looking for Tests related with service_uuid: ${serviceUuid}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def matchedTests = [] as HashSet<Object>
        def packs = gatekeeper.getPackageByUuid(serviceUuid)
        if(packs != null){
            packs.each { pack ->
                if (pack == null) {
                  tangoLoggerType = "I";
                  tangoLoggerOperation = "NetworkService.findTestsByService";
                  tangoLoggerMessage = ("pack null!!");
                  tangoLoggerStatus = "200";
                  tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
                }
                pack.pd.package_content.each { resource ->
                    if (resource.get('content-type') == 'application/vnd.5gtango.nsd'
                            || resource.get('content-type') == 'application/vnd.etsi.osm.nsd') {
                        def testing_tag = resource.get('testing_tags')
                        testing_tag.each { tt ->
                            tangoLoggerType = "I";
                            tangoLoggerOperation = "NetworkService.findTestsByService";
                            tangoLoggerMessage = ("including tests with tag: ${tt}");
                            tangoLoggerStatus = "200";
                            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                            matchedTests << findTestsByTag(tt)
                        }
                    }
                }
            }
        }
        new ArrayList(matchedTests)
    }

    List findTestsByTag(def tag){
        tangoLoggerType = "I";
        tangoLoggerOperation = "NetworkService.findTestsByTag";
        tangoLoggerMessage = ("Looking for Tests with tag: ${tag}");
        tangoLoggerStatus = "200";
        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

        def matchedTests = [] as HashSet<Object>
        def packs = gatekeeper.getPackageByTag(tag)
        if(packs != null){
            tangoLoggerType = "I";
            tangoLoggerOperation = "NetworkService.findTestsByTag";
            tangoLoggerMessage = ("packs size: ${packs.size()}");
            tangoLoggerStatus = "200";
            tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

            packs.each { pack ->
                if (pack == null) {
                  tangoLoggerType = "I";
                  tangoLoggerOperation = "NetworkService.findTestsByService";
                  tangoLoggerMessage = ("pack null!!");
                  tangoLoggerStatus = "200";
                  tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)
                }
                pack.pd.package_content.each { resource ->
                    if (resource.get('content-type') == 'application/vnd.5gtango.tstd'
                            || resource.get('content-type') == 'application/vnd.etsi.osm.tstd') {
                        tangoLoggerType = "I";
                        tangoLoggerOperation = "NetworkService.findTestsByTag";
                        tangoLoggerMessage = ("including tests that match with test uuid: ${resource.uuid}");
                        tangoLoggerStatus = "200";
                        tangoLogger.log(tangoLoggerType, tangoLoggerOperation, tangoLoggerMessage, tangoLoggerStatus)

                        matchedTests << gatekeeper.getTest(resource.uuid)
                    }
                }
            }
        }
        new ArrayList(matchedTests)
    }
}
