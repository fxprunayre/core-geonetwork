package org.fao.geonet.kernel.search.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.TypeTokenFilter;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;

/**
 * Synonym analyzer using {@link SolrSynonymParser}.
 * 
 * Synonym analysis is using a {@link KeywordAnalyzer}.
 * 
 * @author francois
 *
 */
public class SynonymAnalyzer extends Analyzer{
    private SynonymMap synonymMap;
    private boolean ignoreCase;
    private boolean keepOnlyMatchingTerms;
    private boolean useWhiteList = true;
    private Set<String> keepTypes = new HashSet<String>();
    
    /**
     * 
     * @param file  The file containing synonyms definitions
     * @param ignoreCase
     * @param keepOnlyMatchingTerms True to not take into account terms not matching any of the synonyms
     */
    public SynonymAnalyzer (File file, boolean ignoreCase, boolean keepOnlyMatchingTerms) {
        this.ignoreCase = ignoreCase;
        this.keepOnlyMatchingTerms = keepOnlyMatchingTerms;
        this.keepTypes.add(SynonymFilter.TYPE_SYNONYM);
        
        System.out.println(String.format("SynonymAnalyzer ignoring case: %s, with matching terms only: %s", 
                this.ignoreCase, this.keepOnlyMatchingTerms));
        
        
        boolean dedup = false, expand = false;
        SolrSynonymParser sp = new SolrSynonymParser(dedup, expand, new KeywordAnalyzer());
        
        try {
            if (file != null) {
                InputStream stream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(stream);
                sp.add(reader);
                this.synonymMap = sp.build();
            } else {
                // TODO : Warning
            }
        } catch (IOException e) {
            // TODO : Warning
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO : Warning
            e.printStackTrace();
        }
        System.out.println(String.format(" * synonym map contains %s words", this.synonymMap.words.size()));
    }
    
    
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        // Based on keyword analyzer - TODO : add config for the Analyzer to use
        final Tokenizer source = new KeywordTokenizer(reader);
        
        // Search for synonyms
        TokenStream result = new SynonymFilter(source, this.synonymMap, this.ignoreCase);
        
        // Type filter to only keep terms matching one synonym
        if (this.keepOnlyMatchingTerms) {
            result = new TypeTokenFilter(true, result, this.keepTypes, true);
        }
        
        return new TokenStreamComponents(source, result);
    }
}
