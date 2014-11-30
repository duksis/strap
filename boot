#!/bin/sh
function need() {
  fun=$1; shift; args=$*
  message="$fun $args setup: "
  status="present"
  if ! $( check_$fun $* ); then
    $fun $*
    if [ $? -eq 0 ]; then
      status="done"
    else
      status="failed"
    fi
    wait_for check_$fun $args
  fi
  color_echo "$message" $status
}
function wait_for() {
  fun=$1; shift
  for i in {1..100}; do
    sleep 5
    $( $fun $* ) && break
  done
}
function color_echo() {
  red='\033[0;31m';green='\033[0;32m';white='\033[1;37m';NC='\033[0m' # No Color
  if [ "$2" = "present" ]; then
    status="${white}$2"
  elif [ "$2" = "done" ]; then
    status="{green}$2"
  else
    status="${red}$2"
  fi
  echo "$1 $status${NC}"
}
function check_directory() { path=$1; [ -d $path ]; }
function directory() { path=$1; mkdir $path; }
function check_commandline_tools() {
  xcode_path=$(xcode-select -p)
  [ "$xcode_path" = '/Library/Developer/CommandLineTools' ]
}
function commandline_tools() {
  # https://gist.github.com/brysgo/9007731
  # jacobsalmela.com
  sudo sqlite3 /Library/Application\ Support/com.apple.TCC/TCC.db "INSERT or REPLACE INTO access VALUES('kTCCServiceAccessibility','com.apple.RemoteDesktopAgent',1,1,1,NULL)"
  sudo sqlite3 /Library/Application\ Support/com.apple.TCC/TCC.db "INSERT or REPLACE INTO access VALUES('kTCCServiceAccessibility','com.apple.Terminal',0,1,1,NULL)"
  sudo sqlite3 /Library/Application\ Support/com.apple.TCC/TCC.db "INSERT or REPLACE INTO access VALUES('kTCCServiceAccessibility','/user/bin/osascript',1,1,1,NULL)"
  xcode-select --install
  sleep 1
  osascript << EOF
tell application "System Events"
  tell process "Install Command Line Developer Tools"
    keystroke return
    click button "Agree" of window "License Agreement"
  end tell
end tell
EOF
}
function check_homebrew() {
  [ "$(which brew)" = "/usr/local/bin/brew" ]
}
function homebrew() {
  ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
}
function check_ruby_version_manager() {
  [ -f ~/.rvm/scripts/rvm ]
}
function ruby_version_manager() {
  \curl -sSL https://get.rvm.io | bash -s latest
  source ~/.rvm/scripts/rvm
}
function check_brew_package() {
  [ "$(brew info $1 | sed '3q;d')" != "Not installed" ]
}
function brew_package() {
  echo "brewing package $1"
}
function check_clone() {
  [ -d $2 ]
}
function clone() {
  git clone $1 $2
}
need directory ~/code
need commandline_tools
need ruby_version_manager
need homebrew
need brew_package tmux
need clone https://github.com/duksis/strap.git ~/code/strap

[ -f ~/code/strap/install ] && ~/code/strap/install
