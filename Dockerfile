FROM gliderlabs/alpine:3.1
MAINTAINER SequenceIQ

RUN apk-install openjdk7 bash

ADD . /cloudbreak

RUN cd /cloudbreak && ./gradlew clean build

RUN rm -rf /cloudbreak/.git

CMD ["/cloudbreak/core/run_cloudbreak.sh"]