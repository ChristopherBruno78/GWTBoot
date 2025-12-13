all:
	ant clean dist-dev -Dgwt.version=$(version)

install:
	maven/push-gwtproject.sh 	
