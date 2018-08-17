package com.camunda.consulting.jobhandler;

import com.camunda.consulting.PayloadGenerator;
import com.camunda.consulting.SimulationExecutor;
import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

public class FireEventJobHandlerTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
    Mocks.register("generator", new PayloadGenerator());
  }

  @Deployment(resources = "createEventSubscriptionTestModel.bpmn")
  @Test
  public void shouldFireMessageThenSignal() {

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("createEventSubscription");
    assertThat(processInstance).isStarted().isWaitingAt("UT");

    SimulationExecutor.execute(DateTime.now().toDate(), DateTime.now().plusHours(1).toDate());

    assertThat(processInstance).hasPassedInOrder("messageFired", "signalFired", "timerFired");

  }

  @Deployment(resources = "eventSubProcessModel.bpmn")
  @Test
  public void shouldFireForEventSubProcesses() {

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("eventSubProcess");
    assertThat(processInstance).isStarted().isWaitingAt("UT");

    SimulationExecutor.execute(DateTime.now().toDate(), DateTime.now().plusHours(1).toDate());

    assertThat(processInstance).hasPassedInOrder("Message_Start", "Signal_Start").hasNotPassed("EndEvent_0nzq8ia");

  }

}