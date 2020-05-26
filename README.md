# All Hail AK/AS
+ Access AWS Console in a passwordless way by using Access Key.
    - e.g: `./getConsole.sh ec2 profile1`
+ Rotate Access Keys in `~/.aws/credentials`.
    - e.g: `./rotateAk.sh profile1`

## Build 
+ Prerequisites: Maven.
+ Run `mvn package` to build.

## Test
+ Prerequisites: AWSCLI compatible configuration files in place such as `~/.aws/config`, `~/.aws/credentials`.
+ Run `./test.sh` to test.

## Run
+ Prerequisites: AWSCLI compatible configuration files in place such as `~/.aws/config`, `~/.aws/credentials`.
+ Execute `./getConsole.sh [<service-name>] <profile>` to access the AWS console by configured profile.
+ For example: 
    - Go to default console page by using default profile: `./getConsole default`.
    - Go to ec2 console page by using profile named with profile1: `./getConsole.sh ec2 profile1`.
