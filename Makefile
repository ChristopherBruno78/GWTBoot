all:
	ant clean dist-dev -Dgwt.version=$(version)

install:
	maven/push-gwtproject.sh

clean:
	rm -rf build
	rm -rf dist
	rm -rf boot/target
	rm -rf boot/cli-tool/target
