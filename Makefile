all:
	ant clean dist-dev -Dgwt.version=$(version)

install:
	maven/push-gwtproject.sh
	cd boot; make

clean:
	rm -rf build
	rm -rf boot/target
	rm -rf boot/cli-tool/target
