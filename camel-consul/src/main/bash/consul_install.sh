#!/usr/bin/env bash

DEFAULT_CONSUL_VERSION="0.6.4"

function usage {
    cat <<-END >&2
    USAGE: $0
            -p CONSUL_PATH     # Consul installation path
            -v CONSUL_VERSION  # Consul version, default ${DEFAULT_CONSUL_VERSION}
            -h                 # this usage message
END
    exit
}

################################################################################
#
################################################################################

while getopts p:v:h opt
do
    case $opt in
    p)  export CONSUL_PATH=$OPTARG ;;
    v)  export CONSUL_VERSION=$OPTARG ;;
    h|?) usage ;;
    esac
done

shift $(( $OPTIND - 1 ))

if [[ ! ${CONSUL_PATH} ]]; then
    echo "Warning, CONSUL_PATH is required"
    usage
fi

if [[ ! ${CONSUL_VERSION} ]]; then
   CONSUL_VERSION=${DEFAULT_CONSUL_VERSION}
fi

################################################################################
#
################################################################################

URL="https://releases.hashicorp.com/consul/${1}/consul_${1}_linux_amd64.zip"
TMP="/tmp/consul.zip"

if [ -d $CONSUL_PATH ]; then
    rm -rf $CONSUL_PATH
fi

if [ ! -d $CONSUL_PATH ]; then
    mkdir -p $CONSUL_PATH
fi

wget $URL -O $TMP
unzip -d $CONSUL_PATH $TMP

rm $TMP
