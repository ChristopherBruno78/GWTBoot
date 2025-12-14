#!/bin/bash

# GWT Boot Installation Script
# Installs GWT SDK to Maven and the CLI tool system-wide or to user's local directory

GWT_BOOT_URL="https://gwtboot.cocoawerks.com"

wget -r -nH -x ${GWT_BOOT_URL}/gwt-boot.zip

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================================================
# Maven deployment functions
# ============================================================================

# Locate Maven exe
export MAVEN_BIN=${MAVEN_BIN:=`which mvn`}
if [ -z "$MAVEN_BIN" ]; then
  echo "mvn not found. Add mvn to PATH or set MAVEN_BIN."
  exit
fi
echo Using $MAVEN_BIN

function set-random-dir() {
  export RANDOM_DIR=/tmp/random-dir-$RANDOM$RANDOM$RANDOM$RANDOM
  rm -rf $RANDOM_DIR
  mkdir -p $RANDOM_DIR
}

function maven-deploy-file() {
  local mavenRepoUrl=$1
  shift
  local mavenRepoId=$1
  shift
  local curFile=$1
  shift
  local pomFile=$1
  shift

  if [ $# -ne 0 ] && [ -n "$1" ]; then
    [ -f "$1" ] && local javadoc="-Djavadoc=$1"
    shift
  fi
  if [ $# -ne 0 ] && [ -n "$1" ]; then
    [ -f "$1" ] && local sources="-Dsources=$1"
    shift
  fi

  if [[ "$curFile" == "" ]]; then
    echo "ERROR: Unable to deploy $artifactId in repo! Cannot find corresponding file!"
    return 1
  fi

  local cmd="";
  if [[ "$mavenRepoUrl" == "install" ]]; then
    echo "Installing $curFile into local maven repository cache"
    cmd="$MAVEN_BIN \
           install:install-file
            -Dfile=$curFile \
            -DpomFile=$pomFile \
            $javadoc \
            $sources \
            -q"
  elif [[ "$gpgPassphrase" != "" ]]; then
    echo "Signing and Deploying $curFile to $mavenRepoUrl"
    cmd="$MAVEN_BIN \
           org.apache.maven.plugins:maven-gpg-plugin:1.4:sign-and-deploy-file \
            -Dfile=$curFile \
            -Durl=$mavenRepoUrl \
            -DrepositoryId=$mavenRepoId \
            -DpomFile=$pomFile \
            $javadoc \
            $sources \
            -q \
            -Dgpg.passphrase=\"$gpgPassphrase\""
  else
    echo "GPG passphrase not specified; will attempt to deploy files without signing"
    cmd="$MAVEN_BIN \
           org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
            -Dfile=$curFile \
            -Durl=$mavenRepoUrl \
            -DrepositoryId=$mavenRepoId \
            -DpomFile=$pomFile \
            $javadoc \
            $sources \
            -q"
  fi
  eval $cmd
}

function finishAndCleanup () {
  if [[ $thereHaveBeenErrors ]]; then
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "WARNING: Errors while deploying files, examine output above."
    echo "Leaving intermediate files at:"
    echo "$RANDOM_DIR"
    find $pomDir -name pom.xml -o -name pom.xml.asc
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  else
    # Clean up
    rm -rf $RANDOM_DIR
    # Remove POMs & ASCs, leaving only templates
    find $pomDir -name pom.xml -o -name pom.xml.asc -delete
  fi
}

function die () {
  thereHaveBeenErrors=1
  if [[ "$continueOnErrors" != "y" ]]; then
    read -e -p"Error while deploying, ignore errors? (y/N): " continueOnErrors
    if [[ "$continueOnErrors" != "y" ]]; then
      finishAndCleanup
      exit 1
    fi
  fi
}

function warnJavaDoc () {
  echo "WARNING: Could not deploy JavaDoc for $1. Continuing"
}

function maven-gwt() {
  local gwtMavenVersion=$1
  shift
  local gwtSdkArchive=$1
  shift
  local mavenRepoUrl=$1
  shift
  local mavenRepoId=$1
  shift

  # Auto-detect zip file if not specified
  if [[ "$gwtSdkArchive" == "" ]]; then
    local ZIP_FILES=($SCRIPT_DIR/gwt-boot/*.zip)
    if [[ -f "${ZIP_FILES[0]}" ]]; then
      gwtSdkArchive="${ZIP_FILES[0]}"
      echo "Auto-detected GWT SDK archive: $gwtSdkArchive"
    else
      echo "ERROR: No zip file found in $SCRIPT_DIR/gwt-boot and gwtSdkArchive not specified"
      exit 1
    fi
  fi

  if [[ "$mavenRepoUrl" == "" ]]; then
    echo "ERROR: Incorrect parameters to maven-gwt"
    exit 1
  fi

  if [[ "$mavenRepoId" == "" ]]; then
    if [[ "$mavenRepoUrl" != "install" ]] && [[ "`expr match $mavenRepoUrl "file://"`" == 0 ]]; then
   	echo "ERROR: maven-gwt: mavenRepoId is not specified, and the mavenRepoUrl is not local (does not start with file://)"
    	exit 1
    fi
   # set a dummy repo id
   mavenRepoId=local
  fi

  set-random-dir
  echo "Unzipping $gwtSdkArchive to $RANDOM_DIR"
  unzip -q $gwtSdkArchive -d $RANDOM_DIR || exit 1

  GWT_EXTRACT_DIR=`ls $RANDOM_DIR | tail -n1`
  GWT_EXTRACT_DIR=$RANDOM_DIR/$GWT_EXTRACT_DIR

  JAVADOC_FILE_PATH=$RANDOM_DIR/gwt-javadoc.jar
  [ -d $GWT_EXTRACT_DIR/doc/javadoc ] && jar cf $JAVADOC_FILE_PATH -C $GWT_EXTRACT_DIR/doc/javadoc .

  # Generate POMs with correct version
  for template in `find $pomDir -name pom-template.xml`
  do
    dir=`dirname $template`
    pushd $dir > /dev/null
    sed -e "s|\${gwtVersion}|$gwtMavenVersion|g" pom-template.xml >pom.xml
    popd > /dev/null
  done

  gwtLibs='dev user servlet servlet-jakarta codeserver'

  echo "Removing bundled third-parties from gwt-dev"
  zip -q $GWT_EXTRACT_DIR/gwt-dev.jar --copy --out $GWT_EXTRACT_DIR/gwt-dev-trimmed.jar \
      "com/google/gwt/*"
  mv $GWT_EXTRACT_DIR/gwt-dev-trimmed.jar $GWT_EXTRACT_DIR/gwt-dev.jar
  echo "Removing bundled third-parties from gwt-user"
  zip -q $GWT_EXTRACT_DIR/gwt-user.jar --copy --out $GWT_EXTRACT_DIR/gwt-user-trimmed.jar \
      "com/google/gwt/*" "com/google/web/bindery/*" "javaemul/*" \
      "javax/validation/*" "org/hibernate/validator/*" \
      "org/w3c/flute/*"
  mv $GWT_EXTRACT_DIR/gwt-user-trimmed.jar $GWT_EXTRACT_DIR/gwt-user.jar

  for i in $gwtLibs
  do
    CUR_FILE=`ls $GWT_EXTRACT_DIR/gwt-${i}.jar`

    # Get rid of the INDEX.LIST file, since it's going to be out of date
    # once we rename the jar files for Maven
    if unzip -l $CUR_FILE META-INF/INDEX.LIST >/dev/null; then
      echo "Removing INDEX.LIST from gwt-${i}"
      zip -d $CUR_FILE META-INF/INDEX.LIST
    fi

    SOURCES_FILE=$GWT_EXTRACT_DIR/gwt-${i}-sources.jar
    if unzip -l $CUR_FILE '*.java' >/dev/null; then
      zip -q $CUR_FILE --copy --out $SOURCES_FILE "*.java"
    fi
  done

  # push parent poms
  maven-deploy-file $mavenRepoUrl $mavenRepoId $pomDir/gwt/pom.xml $pomDir/gwt/pom.xml

  for i in $gwtLibs
  do
    CUR_FILE=`ls $GWT_EXTRACT_DIR/gwt-${i}.jar`
    gwtPomFile=$pomDir/gwt/gwt-$i/pom.xml
    SOURCES_FILE=gwt-${i}-sources.jar
    SOURCES_PATH_FILE=$GWT_EXTRACT_DIR/$SOURCES_FILE
    # If there are no sources, fail, this is a requirement of maven central
    if [ ! -f $SOURCES_PATH_FILE ]; then
      echo "ERROR: sources jar not found for $i"
      exit 1
    fi

    maven-deploy-file $mavenRepoUrl $mavenRepoId "$CUR_FILE" $gwtPomFile "$JAVADOC_FILE_PATH" "$SOURCES_PATH_FILE" || die
  done

  # Deploy RequestFactory jars
  maven-deploy-file $mavenRepoUrl $mavenRepoId $pomDir/requestfactory/pom.xml $pomDir/requestfactory/pom.xml || die

  for i in client server apt server-jakarta
  do
    maven-deploy-file $mavenRepoUrl $mavenRepoId $GWT_EXTRACT_DIR/requestfactory-${i}.jar $pomDir/requestfactory/${i}/pom.xml \
        $JAVADOC_FILE_PATH $GWT_EXTRACT_DIR/requestfactory-${i}-src.jar \
         || die
  done

  finishAndCleanup
}

# ============================================================================
# Main Installation Logic
# ============================================================================

echo -e "${BLUE}GWT Boot Installer${NC}"
echo ""

# Extract gwt-boot
if [ -f "$SCRIPT_DIR/gwt-boot.zip" ]; then
  echo "Extracting gwt-boot..."
  unzip -q "$SCRIPT_DIR/gwt-boot.zip" -d "$SCRIPT_DIR/gwt-boot"
  echo ""

  # Build the boot project archetype
  if [ -d "$SCRIPT_DIR/gwt-boot/boot" ]; then
    echo "Building boot project..."
    cd "$SCRIPT_DIR/gwt-boot/boot"
    mvn clean install
    cd "$SCRIPT_DIR"
    echo ""
  fi
fi

export pomDir=$SCRIPT_DIR/gwt-boot/poms

# Step 1: Install GWT SDK to Maven
echo -e "${BLUE}Step 1: Installing GWT SDK to Maven repository${NC}"
echo ""

# Auto-detect zip file
ZIP_FILES=($SCRIPT_DIR/gwt-boot/*.zip)
if [[ ! -f "${ZIP_FILES[0]}" ]]; then
  echo -e "${RED}ERROR: No GWT SDK zip file found in $SCRIPT_DIR/gwt-boot${NC}"
  exit 1
fi

gwtSdkArchive="${ZIP_FILES[0]}"
zipBasename=$(basename "$gwtSdkArchive" .zip)

# Try to extract version from filename (e.g., gwt-2.11.0.zip -> 2.11.0)
if [[ "$zipBasename" =~ gwt-(.+) ]]; then
  gwtMavenVersion="${BASH_REMATCH[1]}"
else
  echo -e "${RED}ERROR: Cannot determine GWT version from zip filename: $zipBasename${NC}"
  echo "Expected format: gwt-X.Y.Z.zip"
  exit 1
fi

echo "Installing GWT $gwtMavenVersion to local .m2 repository"
echo "Source: $gwtSdkArchive"
echo ""

# Install to local .m2 repository
mavenRepoUrl="install"
mavenRepoId=""

maven-gwt "$gwtMavenVersion" "$gwtSdkArchive" "$mavenRepoUrl" "$mavenRepoId"

echo ""
echo -e "${GREEN}GWT $gwtMavenVersion installed successfully to Maven!${NC}"
echo ""

# Step 2: Install GWT Boot CLI
echo -e "${BLUE}Step 2: Installing GWT Boot CLI${NC}"
echo ""

JAR_FILE="$SCRIPT_DIR/gwt-boot/gwt-boot-cli.jar"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR file not found at $JAR_FILE${NC}"
    exit 1
fi

# Determine installation type
INSTALL_TYPE=""
INSTALL_DIR=""
BIN_DIR=""

if [ -w "/usr/local/bin" ] && [ -w "/usr/local/lib" ]; then
    # System-wide installation (user has write access)
    INSTALL_TYPE="system"
    INSTALL_DIR="/usr/local/lib/gwt-boot"
    BIN_DIR="/usr/local/bin"
    echo -e "${BLUE}Installing system-wide to /usr/local/${NC}"
elif command -v sudo &> /dev/null; then
    # Ask if user wants to install system-wide with sudo
    echo -e "${YELLOW}System-wide installation requires sudo privileges.${NC}"
    read -p "Install system-wide with sudo? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        INSTALL_TYPE="system-sudo"
        INSTALL_DIR="/usr/local/lib/gwt-boot"
        BIN_DIR="/usr/local/bin"
        echo -e "${BLUE}Installing system-wide to /usr/local/ (with sudo)${NC}"
    else
        INSTALL_TYPE="user"
        INSTALL_DIR="$HOME/.local/lib/gwt-boot"
        BIN_DIR="$HOME/.local/bin"
        echo -e "${BLUE}Installing to user directory ~/.local/${NC}"
    fi
else
    # User-local installation
    INSTALL_TYPE="user"
    INSTALL_DIR="$HOME/.local/lib/gwt-boot"
    BIN_DIR="$HOME/.local/bin"
    echo -e "${BLUE}Installing to user directory ~/.local/${NC}"
fi

echo ""

# Create directories
if [ "$INSTALL_TYPE" = "system-sudo" ]; then
    echo "Creating installation directories..."
    sudo mkdir -p "$INSTALL_DIR"
    sudo mkdir -p "$BIN_DIR"
else
    echo "Creating installation directories..."
    mkdir -p "$INSTALL_DIR"
    mkdir -p "$BIN_DIR"
fi

# Copy JAR file
echo "Installing JAR file..."
if [ "$INSTALL_TYPE" = "system-sudo" ]; then
    sudo cp "$JAR_FILE" "$INSTALL_DIR/gwt-boot-cli.jar"
    sudo chmod 644 "$INSTALL_DIR/gwt-boot-cli.jar"
else
    cp "$JAR_FILE" "$INSTALL_DIR/gwt-boot-cli.jar"
    chmod 644 "$INSTALL_DIR/gwt-boot-cli.jar"
fi

# Create wrapper script
echo "Creating executable wrapper..."
WRAPPER_SCRIPT="$BIN_DIR/gwt-boot"

if [ "$INSTALL_TYPE" = "system-sudo" ]; then
    sudo tee "$WRAPPER_SCRIPT" > /dev/null << 'EOF'
#!/bin/bash
# GWT Boot CLI wrapper script
exec java -jar /usr/local/lib/gwt-boot/gwt-boot-cli.jar "$@"
EOF
    sudo chmod 755 "$WRAPPER_SCRIPT"
else
    cat > "$WRAPPER_SCRIPT" << EOF
#!/bin/bash
# GWT Boot CLI wrapper script
exec java -jar "$INSTALL_DIR/gwt-boot-cli.jar" "\$@"
EOF
    chmod 755 "$WRAPPER_SCRIPT"
fi

echo "Cleaning up..."
rm -rf gwt-boot*

echo ""
echo -e "${GREEN}Installation complete!${NC}"
echo ""

# Check if BIN_DIR is in PATH
if [[ ":$PATH:" == *":$BIN_DIR:"* ]]; then
    echo -e "${GREEN}$BIN_DIR is already in your PATH.${NC}"
    echo ""
    echo "You can now use 'gwt-boot' from anywhere:"
    echo "  gwt-boot --version"
    echo "  gwt-boot boot myapp"
else
    echo -e "${YELLOW}Warning: $BIN_DIR is not in your PATH.${NC}"
    echo ""
    echo "To use 'gwt-boot' from anywhere, add this directory to your PATH:"
    echo ""

    # Detect shell and provide appropriate instructions
    if [ -n "$BASH_VERSION" ]; then
        echo "  echo 'export PATH=\"\$PATH:$BIN_DIR\"' >> ~/.bashrc"
        echo "  source ~/.bashrc"
    elif [ -n "$ZSH_VERSION" ]; then
        echo "  echo 'export PATH=\"\$PATH:$BIN_DIR\"' >> ~/.zshrc"
        echo "  source ~/.zshrc"
    else
        echo "  export PATH=\"\$PATH:$BIN_DIR\""
        echo ""
        echo "Add the above line to your shell's rc file (~/.bashrc, ~/.zshrc, etc.)"
    fi

    echo ""
    echo "Or run directly using the full path:"
    echo "  $WRAPPER_SCRIPT --version"
fi

echo ""
echo -e "${BLUE}Installed files:${NC}"
echo "  JAR:     $INSTALL_DIR/gwt-boot-cli.jar"
echo "  Wrapper: $WRAPPER_SCRIPT"
echo ""
echo -e "${BLUE}To uninstall, run:${NC}"
echo "  gwt-boot uninstall"
echo ""
