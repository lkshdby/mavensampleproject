include Make.rules

TARGET_PREFIX=target
BUILD_TOPDIR=$(shell pwd)

all: create_build_env copy_source build1 copy_sql copy_war buildrpm listrpm

build1:
	@echo "Building CPE sources..."
	make -C ${TARGET_PREFIX}/SOURCES/packages install

buildrpm:
	@echo "Building the CPE RPM ..."
	$(RPMBUILD) --define "_topdir ${TARGET_PREFIX}" --define 'noclean 1' -bb ${TARGET_PREFIX}/SPECS/cpe.spec --buildroot ${BUILD_TOPDIR}/${TARGET_PREFIX}/BUILD/ 

listrpm:
	@echo "Listing the generated rpms ..."
	ls -ltR ${TARGET_PREFIX}/RPMS/

copy_source:
	@echo "Copying the source over to the SOURCES directory" 
	cp -r ${TARGET_PREFIX}/classes/packages ${TARGET_PREFIX}/SOURCES/

copy_war:
	mkdir -p ${TARGET_PREFIX}/BUILD/$(PROD_PATH)
	cp -r ${TARGET_PREFIX}/cpe*.war ${TARGET_PREFIX}/BUILD/$(PROD_PATH)/cpe.war
	
copy_sql:
	mkdir -p ${TARGET_PREFIX}/BUILD/$(PROD_PATH)/sql
	cp -r ${TARGET_PREFIX}/classes/create_db_mysql.sql ${TARGET_PREFIX}/BUILD/$(PROD_PATH)/sql
	cp -r ${TARGET_PREFIX}/classes/r2_db_migration.sql ${TARGET_PREFIX}/BUILD/$(PROD_PATH)/sql
	cp -r ${TARGET_PREFIX}/classes/cpe_ui_dump.sql ${TARGET_PREFIX}/BUILD/$(PROD_PATH)/sql
	cp -r ${TARGET_PREFIX}/classes/dgw_db_changes.sql ${TARGET_PREFIX}/BUILD/$(PROD_PATH)/sql

create_build_env:
	@echo "Creating the build environment on the build machine ..."
	mkdir -p ${TARGET_PREFIX}/BUILD 
	mkdir -p ${TARGET_PREFIX}/RPMS 
	mkdir -p ${TARGET_PREFIX}/SOURCES 
	mkdir -p ${TARGET_PREFIX}/SPECS 
	mkdir -p ${TARGET_PREFIX}/SRPMS

clean:
	@echo "Cleaning the CPE RPM build environment, removing the directories ..." 
	rm -rf target/BUILD/*
	rm -rf target/RPMS/*
	rm -rf target/SOURCES/*
	rm -rf target/SPECS/*
	rm -rf target/SRPMS/*
	

