for f in `ls -l *.{jpeg,gif,png,jpg}`;
do
 convert $f -resize 96x96\> ./miniatures/$f
done
