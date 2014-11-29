#!/bin/sh
function need() {
  fun=$1; shift; args=$*
  message="$fun $args creation: "
  status="${white}present"
  check_$fun $*
  if [ $? -ne 0 ]; then
    $fun $*
    if [ $? -eq 0 ]; then
      status="${green}done"
    else
      status="${red}failed"
    fi
  fi
  color_echo "$message" $status
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
function check_directory() {
  path=$1
  if [ -d $path ]; then
    return 0
  else
    return 1
  fi
}
function directory() { path=$1; mkdir $path; }
function check_commandline_tools() {
  xcode_path=$(xcode-select -p)
  if [ "$xcode_path" = '/Library/Developer/CommandLineTools' ]; then
    return 0
  else
    return 1
  fi
}
function commandline_tools() {
  # https://gist.github.com/brysgo/9007731
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
function check_clone() {
  if [ -d $2 ]; then
    return 0
  else
    return 1
  fi
}
function clone() {
  git clone $1 $2
}
need directory ~/code
need commandline_tools
need clone https://github.com/duksis/strap.git ~/code/strap
[ -f ~/code/strap/install ] && ~/code/strap/install
