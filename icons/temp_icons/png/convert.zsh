#!/bin/zsh

declare -a COLORS
COLORS=(
    "red"
    "yellow"
    "white"
    "orange"
    "pink"
    "purple"
    "blue"
    "black"
    "green"
)
declare -r COLORS
mkdir -p ./colorized

for file in *.png; do
    for color in $COLORS; do
        name=$file:r;
        if [[ $color -eq "white" ]]; then
            convert $file -fill "#cccccc" -opaque black ./colorized/${name}_white.png
        elif [[ $color -eq "yellow" ]]; then
            convert $file -fill "#e3e100" -opaque black ./colorized/${name}_yellow.png
        else
            convert $file -fill $color -opaque black ./colorized/${name}_${color}.png
        fi
    done;
done;

