import json
import argparse

from enum import Enum

# TODO: Import create_input

class Strategy(Enum):
    RANDOM_BORDER = 'RANDOM_BORDER'
    RANDOM = 'RANDOM'
    NEAREST_BORDER = 'NEAREST_BORDER'
    DISTRIBUTED_BORDER = 'DISTRIBUTED_BORDER'

    def __str__(self):
        return self.value

parser = argparse.ArgumentParser(description='Create input file')

# Add the arguments
parser.add_argument('--size-x', type=int, required=False, default=20)
parser.add_argument('--size-y', type=int, required=False, default=20)
parser.add_argument('--idling-zones', type=int, required=True)
parser.add_argument('--drop-zones', type=int, required=True)
parser.add_argument('--robots', type=int, required=True)
parser.add_argument('--strategy', type=Strategy, required=True)

args = parser.parse_args()

print(args.idling_zones)
print(args.drop_zones)
print(args.strategy)
print(args.robots)

size_x = args.size_x
size_y = args.size_y
idling_zones = args.idling_zones
drop_zones = args.drop_zones
robots = args.robots

# Basic shape
input = {
    "sizeX": size_x,
    "sizeY": size_y,
    "coordinates": {
         "drop": [],
         "idling": [],
         "robot": []
    },
    # List of lists
    "orders": []
}

def generate_robot_coordinates(num_robots):
    # Place robots at random free places
    print("Not implemented yet")

    return []

def generate_drop_zone_coordinates(num_drop_zones):
    # Place drop zones
    print("Not implemented yet")

    return []

def generate_orders():
    orders = []
    return orders

idling_zone_coordinates = []

if args.strategy == Strategy.RANDOM:
    idling_zone_coordinates = zip(random.sample(range(0, size_x), idling_zones),  random.sample(range(0, size_y), idling_zones))
elif args.strategy == Strategy.RANDOM_BORDER:
    print("Hello World")

input["orders"] = generate_orders()
input["coordinates"]["idling"] = idling_zone_coordinates
input["coordinates"]["robots"] = generate_robot_coordinates(robots)
input["coordinates"]["drop_zones"] = generate_drop_zone_coordinates(drop_zones)

print(json.dumps(input))

print("Creating input file..")