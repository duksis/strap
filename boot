#!/bin/sh
function need() {
  fun=$1; shift
  message="$fun $* setup: "
  status="present"
  if !( check_$fun "$1" "$2" ); then
    $fun "$1" "$2" "$3"
    if [ $? -eq 0 ]; then
      status="done"
    else
      status="failed"
    fi
    wait_for check_$fun "$1" "$2"
  fi
  color_echo "$message" $status
}
function dont() {
  shift; # remove the 'need' keyword
  fun=$1; shift
  message="$fun $* droping: "
  status="not present"
  if ( check_$fun "$1" "$2" ); then
    no_$fun "$1" "$2" "$3"
    if [ $? -eq 0 ]; then
      status="done"
    else
      status="failed"
    fi
    wait_for_not check_$fun "$1" "$2"
  fi
  color_echo "$message" "$status"

}
function wait_for() {
  fun=$1; shift
  for i in {1..100}; do
    sleep 5
    $( $fun "$1" "$2" ) && break
  done
}
function wait_for_not() {
  fun=$1; shift
  for i in {1..100}; do
    sleep 5
    $( ! $fun "$1" "$2" ) && break
  done
}
function color_echo() {
  red='\033[0;31m';green='\033[0;32m';white='\033[1;37m';NC='\033[0m' # No Color
  if [ "$2" = "present" -o "$2" = "not present" ]; then
    status="${white}$2"
  elif [ "$2" = "done" ]; then
    status="${green}$2"
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
  [ "$(brew info $1 | grep '^Not installed')" != "Not installed" ]
}
function brew_package() { brew update && brew install $1; }
function no_brew_package() { brew uninstall $1; }
function check_brew_cask_package() {
  [ "$(brew cask info $1 | grep '^Not installed')" != "Not installed" ]
}
function brew_cask_package() { brew cask update && brew cask install $1; }
function no_brew_cask_package() { brew cask uninstall $1; }
function check_clone() { [ -d $2 ]; }
function clone() { git clone $1 $2; }
function no_clone() { rm -fr $2; }
function check_app_from_archive() { [ -d "/Applications/$1.app" ]; }
function app_from_archive() {
  ([ -f /tmp/$1.zip ] || curl -S $2 > /tmp/$1.zip) && \
  unzip /tmp/$1.zip -d /tmp/ && \
  rm /tmp/$1.zip && \
  mv /tmp/$1.app /Applications/
}
function no_app_from_archive() { rm -fr "/Applications/$1.app"; }
function check_app_from_image() { [ -d "/Applications/$1.app" ]; }
function app_from_image() {
  ([ -f "/tmp/$1.dmg" ] || curl -S $2 > "/tmp/$1.dmg") && \
  hdiutil attach "/tmp/$1.dmg" && \ sudo cp -r "/Volumes/$1/$1.app" /Applications/ && \ hdiutil detach "/Volumes/$1" && \
  rm "/tmp/$1.dmg"
}
function no_app_from_image() { rm -fr "/Applications/$1.app"; }
function check_rvm_ruby() { [ "$(rvm list | grep $1 | cut -d' ' -f2)" = "$1" ]; }
function rvm_ruby() { rvm install $1; }
function no_rvm_ruby() { rvm uninstall $1; }
function install_dotfiles() {
  old_pwd=`pwd`; cd $1; rake install; cd $old_pwd
}
function check_install_dotfiles() { [ -L ~/.bash_profile ] ;}
function no_install_dotfiles() {
  find ~ -type l -exec unlink {} \;
}

need directory ~/code
need commandline_tools
need ruby_version_manager
need rvm_ruby 'ruby-2.1.4'
need homebrew
need brew_package tmux
need brew_package tmux-mem-cpu-load
need brew_package wget
need brew_package nvm
need brew_package percona-server
need brew_package redis
need brew_package qt
need brew_package v8
need brew_package "caskroom/cask/brew-cask"
need brew_cask_package java
need brew_package elasticsearch
need brew_cask_package keepassx
need brew_cask_package alfred
need brew_cask_package tunnelblick
need app_from_archive iTerm https://iterm2.com/downloads/stable/iTerm2_v2_0.zip
need app_from_image 'Google Chrome' https://dl.google.com/chrome/mac/stable/GGRO/googlechrome.dmg
need clone https://github.com/duksis/dotfiles.git ~/code/dotfiles
need clone git://github.com/mururu/exenv.git ~/.exenv
need install_dotfiles ~/code/dotfiles
need clone https://github.com/duksis/strap.git ~/code/strap

[ -f ~/code/strap/install ] && ~/code/strap/install
