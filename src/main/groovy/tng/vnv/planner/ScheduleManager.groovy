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

package tng.vnv.planner

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import tng.vnv.planner.model.TestPlan
import tng.vnv.planner.model.TestSet
import tng.vnv.planner.service.TestService

@Component
class ScheduleManager {

    @Autowired
    TestService testService
    @Autowired
    WorkflowManager workflowManager

    TestSet scheduleNewTestSet(def packageId, def confirmRequired) throws IllegalArgumentException, RestClientException {

        def testSet = testService.buildTestPlansByPackage(packageId, confirmRequired)
        if(testSet == null || testSet.testPlans == null || testSet.testPlans.isEmpty()) {
            throw new IllegalArgumentException("There is no TestPlan built with this PackageUUID: ${packageId}")
        }

        testService.save(testSet)

        new Thread(new Runnable() {
            @Override
            void run() {
                workflowManager.searchForScheduledSet()
            }
        }).start()

        testSet
    }

    TestPlan scheduleNewTestSet(TestPlan tp) {
        testService.save(tp)
    }

    TestPlan update(String uuid, String status) {
        testService.updatePlanStatus(uuid, status)
    }
}
