package dev.mikita.automatewizard.integration;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.mikita.automatewizard.dto.request.AddPluginRequest;
import dev.mikita.automatewizard.dto.request.CreateScenarioRequest;
import dev.mikita.automatewizard.dto.request.TaskRequest;
import dev.mikita.automatewizard.entity.*;
import dev.mikita.automatewizard.environment.plugins.*;
import dev.mikita.automatewizard.repository.UserRepository;
import dev.mikita.automatewizard.service.PluginService;
import dev.mikita.automatewizard.service.ScenarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class ScenarioExecutionTests {
	@LocalServerPort
	private int definedPort;

	@Autowired
	private PluginService pluginService;

	@Autowired
	private ScenarioService scenarioService;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DirtiesContext
	@SuppressWarnings("unchecked")
	void integrationTest_userScenario1Process_success() throws IOException {
		// Create user
		var user = userRepository.save(User.builder().email("test@gmail.com").password("password").build());

		// Plugins WireMock initialization
		List<RemotePlugin> remotePlugins = new ArrayList<>();
		String baseUrl = "http://localhost:" + definedPort;
		remotePlugins.add(new JiraPlugin(baseUrl));
		remotePlugins.add(new SlackPlugin(baseUrl));

		// Start remote plugins
		remotePlugins.forEach(RemotePlugin::start);

		// Register plugins, install and map them
		Map<String, Map<String, Object>> pluginMap = remotePlugins.stream()
				.map(remotePlugin -> {
					Plugin plugin = pluginService.createPlugin(
							AddPluginRequest.builder().url(remotePlugin.getBaseUrl()).build(), user);
					pluginService.installPlugin(plugin.getId(), user);
					Map<String, Object> pluginData = new HashMap<>();
					pluginData.put("plugin", plugin);
					pluginData.put("actions", plugin.getActions().stream()
							.collect(Collectors.toMap(Action::getName, action -> action)));
					pluginData.put("triggers", plugin.getTriggers().stream()
							.collect(Collectors.toMap(Trigger::getName, trigger -> trigger)));
					return Map.entry(plugin.getName().toLowerCase(), pluginData);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Create scenario
		var scenario = scenarioService.createScenario(CreateScenarioRequest.builder().name("Scenario 1").build(), user);

		// Update scenario run type
		scenarioService.updateRunType(scenario.getId(), ScenarioRunType.TRIGGER, user);

		// Update scenario trigger
		Trigger trigger = ((Map<String, Trigger>) pluginMap.get("jira").get("triggers")).get("new-task-trigger");
		ObjectNode triggerData = JsonNodeFactory.instance.objectNode()
				.put("projectId", "858f6f7f-0225-4cf5-8c3a-b0aaa2d4b2ab");

		scenarioService.updateTrigger(scenario.getId(), trigger.getId(), triggerData, user);

		// Update scenario tasks
		List<TaskRequest> tasks = new ArrayList<>();

		tasks.add(TaskRequest.builder()
				.actionId(((Map<String, Action>) pluginMap.get("jira").get("actions")).get("get-project-participants").getId())
				.preprocessor(Base64.getEncoder().encodeToString(
						new ClassPathResource("scenarios/scenario1-preprocessor1.js")
								.getContentAsString(Charset.defaultCharset()).getBytes()))
				.build());

		tasks.add(TaskRequest.builder()
				.actionId(((Map<String, Action>) pluginMap.get("jira").get("actions")).get("assign-task").getId())
				.preprocessor(Base64.getEncoder().encodeToString(
						new ClassPathResource("scenarios/scenario1-preprocessor2.js")
								.getContentAsString(Charset.defaultCharset()).getBytes()))
				.build());

		tasks.add(TaskRequest.builder()
				.actionId(((Map<String, Action>) pluginMap.get("slack").get("actions")).get("send-private-message").getId())
				.preprocessor(Base64.getEncoder().encodeToString(
						new ClassPathResource("scenarios/scenario1-preprocessor3.js")
								.getContentAsString(Charset.defaultCharset()).getBytes()))
				.build());

		tasks.add(TaskRequest.builder()
				.actionId(((Map<String, Action>) pluginMap.get("slack").get("actions")).get("send-private-message").getId())
				.preprocessor(Base64.getEncoder().encodeToString(
						new ClassPathResource("scenarios/scenario1-preprocessor4.js")
								.getContentAsString(Charset.defaultCharset()).getBytes()))
				.build());

		scenarioService.updateTasks(scenario.getId(), tasks, user);

		// Update scenario state
		scenarioService.updateState(scenario.getId(), ScenarioState.ACTIVE, user);

		// Wait for scenario execution
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assertions
		var scenarioExecutions = scenarioService.getExecutions(scenario.getId(), user);

		// Check if scenario execution is not empty
		assertThat(scenarioExecutions).isNotEmpty();
		var scenarioExecution = scenarioExecutions.get(0);

		// Check if scenario execution is completed
		assertThat(scenarioExecution.getState()).isEqualTo(ScenarioExecutionState.COMPLETED);

		// Check if scenario execution has 4 tasks
		var scenarioExecutionTasks = scenarioExecution.getTasks();
		assertThat(scenarioExecutionTasks).hasSize(4);

		// Check if all tasks are successful
		assertThat(scenarioExecutionTasks.stream().allMatch(task -> task.getState() == TaskExecutionState.COMPLETED)).isTrue();
	}

	@Test
	@DirtiesContext
	@SuppressWarnings("unchecked")
	void integrationTest_userScenario2Process_success() throws IOException {
		// Create user
		var user = userRepository.save(User.builder().email("test@gmail.com").password("password").build());

		// Plugins WireMock initialization
		List<RemotePlugin> remotePlugins = new ArrayList<>();
		String baseUrl = "http://localhost:" + definedPort;
		remotePlugins.add(new HTMLPlugin(baseUrl));
		remotePlugins.add(new SMSPlugin(baseUrl));

		// Start remote plugins
		remotePlugins.forEach(RemotePlugin::start);

		// Register plugins, install and map them
		Map<String, Map<String, Object>> pluginMap = remotePlugins.stream()
				.map(remotePlugin -> {
					Plugin plugin = pluginService.createPlugin(
							AddPluginRequest.builder().url(remotePlugin.getBaseUrl()).build(), user);
					pluginService.installPlugin(plugin.getId(), user);
					Map<String, Object> pluginData = new HashMap<>();
					pluginData.put("plugin", plugin);
					pluginData.put("actions", plugin.getActions().stream()
							.collect(Collectors.toMap(Action::getName, action -> action)));
					pluginData.put("triggers", plugin.getTriggers().stream()
							.collect(Collectors.toMap(Trigger::getName, trigger -> trigger)));
					return Map.entry(plugin.getName().toLowerCase(), pluginData);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Create scenario
		var scenario = scenarioService.createScenario(CreateScenarioRequest.builder().name("Scenario 2").build(), user);

		// Update scenario run type
		scenarioService.updateRunType(scenario.getId(), ScenarioRunType.SCHEDULE, user);

		// Update scenario schedule
		scenarioService.updateSchedule(scenario.getId(), "0/2 * * ? * *", user);

		// Update scenario tasks
		List<TaskRequest> tasks = new ArrayList<>();

		tasks.add(TaskRequest.builder()
				.actionId(((Map<String, Action>) pluginMap.get("html").get("actions")).get("get-webpage").getId())
				.preprocessor(Base64.getEncoder().encodeToString(
						new ClassPathResource("scenarios/scenario2-preprocessor1.js")
								.getContentAsString(Charset.defaultCharset()).getBytes()))
				.build());

		tasks.add(TaskRequest.builder()
				.actionId(((Map<String, Action>) pluginMap.get("sms").get("actions")).get("send-sms").getId())
				.preprocessor(Base64.getEncoder().encodeToString(
						new ClassPathResource("scenarios/scenario2-preprocessor2.js")
								.getContentAsString(Charset.defaultCharset()).getBytes()))
				.build());

		scenarioService.updateTasks(scenario.getId(), tasks, user);

		// Update scenario state
		scenarioService.updateState(scenario.getId(), ScenarioState.ACTIVE, user);

		// Waiting for the scheduler to start
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		scenarioService.updateState(scenario.getId(), ScenarioState.INACTIVE, user);

		// Wait for scenario execution
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Assertions
		var scenarioExecutions = scenarioService.getExecutions(scenario.getId(), user);

		// Check if scenario execution is not empty
		assertThat(scenarioExecutions).isNotEmpty();
		var scenarioExecution = scenarioExecutions.get(0);

		// Check if scenario execution is completed
		assertThat(scenarioExecution.getState()).isEqualTo(ScenarioExecutionState.COMPLETED);

		// Check if scenario execution has 4 tasks
		var scenarioExecutionTasks = scenarioExecution.getTasks();
		assertThat(scenarioExecutionTasks).hasSize(2);

		// Check if all tasks are successful
		assertThat(scenarioExecutionTasks.stream().allMatch(task -> task.getState() == TaskExecutionState.COMPLETED)).isTrue();
	}
}
