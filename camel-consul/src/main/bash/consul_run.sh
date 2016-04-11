#!/usr/bin/env bash

DEFAULT_CONSUL_BIND="127.0.0.1"

function usage {
    cat <<-END >&2
    USAGE: $0
            -p CONSUL_PATH     # Consul installation path
            -b CONSUL_BIND     # Consul bind address, default ${DEFAULT_CONSUL_BIND}
            -h                 # this usage message
END
    exit
}

################################################################################
#
################################################################################

while getopts "p:b:h" opt
do
    case $opt in
    p)  export CONSUL_PATH=$OPTARG ;;
    b)  export CONSUL_BIND=$OPTARG ;;
    h|?) usage ;;
    esac
done

shift $(( $OPTIND - 1 ))

if [[ ! ${CONSUL_BIND} ]]; then
   CONSUL_BIND=${DEFAULT_CONSUL_BIND}
fi

################################################################################
#
################################################################################

if [ ! "${CONSUL_PATH+x}" ]; then
    echo "Missing CONSUL_PATH"
    exit -1
else
    rm -rf ${CONSUL_PATH}/runtime

    mkdir -p ${CONSUL_PATH}/runtime/data
    mkdir -p ${CONSUL_PATH}/runtime/config
    mkdir -p ${CONSUL_PATH}/runtime/ui
    mkdir -p ${CONSUL_PATH}/runtime/log

    ${CONSUL_PATH}/consul \
        agent \
        -server \
        -bootstrap \
        -advertise ${CONSUL_BIND} \
        -data-dir ${CONSUL_PATH}/runtime/data \
        -config-dir ${CONSUL_PATH}/runtime/config \
        -ui-dir ${CONSUL_PATH}/runtime/ui \
        -log-level trace 2>&1 > ${CONSUL_PATH}/runtime/log/consul.log &
fi
