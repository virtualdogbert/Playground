/**
 * Just and idea I had bounding around my head, becasue I want to be able to
 * have white list detection for xss, but I don't want to sanitize the conenet,
 * I just want to check it and throw and error.  Current libraries I've found don't
 * cut it like Anti-Samy(no longger supported) and OWASP Java HTML Sanitizer which 
 * leans tward sanitizing, chaning the orginal html, making it hard to detect if 
 * xxs was removed, which leads to false possitives. So my idea it to parse through
 * all the elements and atributes seeing if they are allowed, also I would add checks
 * based on regexs for values. I'm thinking of adapting something like the 
 * htmlPolicy.groovy(orrigally and example from Java HTML Sanitizer), but using
 * groovy maps for configuration.
 */

@Grapes( @Grab('org.jsoup:jsoup:1.8.2'))
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import groovy.transform.*

@CompileStatic
class HtmlBouncer{
    List<String> allowedElements = ['p','b','body']
    List<String> allowedAttributes = ['id', 'class','style']
    void parseHtml(){
        String html = "<p class='classy'>An <a id='5' href='http://example.com/'><b>example</b></a> link.</p>"
        html = "<p class='classy' style='color:blue'>An <b>example</b> link.</p>"
        Document doc = Jsoup.parse(html)
        def elements = doc.body().select("*")

        elements.each{ element ->
            if(!(element.tagName() in allowedElements)){
                System.out.println(element.tagName())
            }
            
            element.attributes().each{ attribute ->
            println attribute.key
                    println attribute.value
                if(!(attribute.key in allowedAttributes)){
                    println attribute.key
                    println attribute.value
                }
            }
        }

    }
    
    public static void main(String[] args){
        Date d = new Date()
        new HtmlBouncer().parseHtml()
        println "${new Date().time - d.time}"
    }
}
