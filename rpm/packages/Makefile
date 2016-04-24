#
# If you create a new file, please modify the following:
# 1. Include path to Make.rules
# 2. subdirs list 
# 3. files list 
# 4. BUILD_PATH 
#
# You should be able to leave everything else untouched 
#
# "make install" will execute the build process from this directory downwards 
# 
include ../../../Make.rules

subdirs = 
files = cpe.spec 

BUILD_PATH=$(TOP)$(TARGET_PREFIX)/BUILD/packages
SHIP_PATH=$(TOP)$(TARGET_PREFIX)/SPECS/

# Leave everything else under this line untouched 

debug:
	@echo "Printing debug, use 'make install' to run build"
	@echo DEBUG BUILD_PATH = $(BUILD_PATH)
	@echo DEBUG SHIP_PATH = $(SHIP_PATH)
	@echo DEBUG files_build_targets = $(files_build_targets)
	@echo DEBUG files_ship_targets = $(files_ship_targets)


subdir_install_targets=${subdirs:%=%.install}
subdir_clean_targets=${subdirs:%=%.clean}

files_build_targets=${files:%=%.install}
files_ship_targets=${files:%=%.ship}

all: install

install: $(subdir_install_targets) prepare_dirs $(files_build_targets) ${files_ship_targets}

$(subdir_install_targets):
	$(MAKE) -C ${@:%.install=%} install

clean: $(subdir_clean_targets)
$(subdir_clean_targets):
	$(MAKE) -C ${@:%.clean=%} clean
	rmdir ${BUILD_PATH}/${@:%.clean=%}

clean: 
	rm -f ${BUILD_PATH}/*
	rm -f ${SHIP_PATH}/*

prepare_dirs:
	$(MKDIR_P) $(BUILD_PATH)
	$(MKDIR_P) $(SHIP_PATH)

$(files_build_targets):
	$(CP) ${@:%.install=%} $(BUILD_PATH)/${@:%.install=%}
	${SED} -i -e 's#__REPLACE_BUILDER_NAME__#$(BUILDER)#g' $(BUILD_PATH)/${@:%.install=%}
	${SED} -i -e 's#__REPLACE_BUILD_DATE__#$(DATE)#g' $(BUILD_PATH)/${@:%.install=%}

$(files_ship_targets):
	$(CP) $(BUILD_PATH)/${@:%.ship=%} $(SHIP_PATH)/

