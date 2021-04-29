current=`pwd`
echo "Copy schemas in $current"
cd schemas
for d in *; do
 if [ -d "$d/src/main/plugin/$d" ]; then
   echo $d
   rm -fr ../web/src/main/webapp/WEB-INF/data/config/schema_plugins/$d
   cp -fr $d/src/main/plugin/$d ../web/src/main/webapp/WEB-INF/data/config/schema_plugins/.
 fi
done

# Clean XSL caching
touch web/src/main/webapp/xslt/ui-metadata/edit/edit.xsl
touch web/src/main/webapp/xslt/services/thesaurus/convert.xsl
touch web/src/main/webapp/xslt/services/subtemplate/convert.xsl
