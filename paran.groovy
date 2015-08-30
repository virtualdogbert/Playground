/**
 * This stack based interview question I saw countring parens and seeing if they are
 * all paired. This is groovy solution I threw together.
 */

int countParenPairs(String string){
    List stack = []
    int count = 0
    string.each{ character ->
        if(character == '('){
            stack.push(character)
        }
        
        if(stack && character == ')'){
            count++
            stack.pop()
        }
    }
    return count
}

boolean parensPaired(String string){
    List stack = []
    boolean allPaired = true
    string.each{ character ->
        if(character == '('){
            stack.push(character)
        }
        
        if(  character == ')' && !stack){
            allPaired = false
            return 
        }
        
        if(stack && character == ')'){
            stack.pop()
        }
        
        
    }
    
    if(stack){
        return false
    }
    
    return allPaired
}

println countParenPairs('(()()))')
println parensPaired('(()()))))')
println parensPaired('(()())')
println parensPaired(')(()())')
println parensPaired('(()())(')
