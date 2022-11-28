# Run actual experiment, will create input files under resources/experiment
experiment = Experiment()

# Each case writes a json file
experiment.addCase(num_agents, idling_zone_distribution, ...)
experiment.addCase(num_agents, idling_zone_distribution, ...)

experiment.dumb()