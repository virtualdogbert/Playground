
//intercepting methods


class Time {
  static def code(closure) {
    def start = System.nanoTime()
    try {
      closure()
    } catch(ex) {
      println "Exception received: $ex"
    }
    def end = System.nanoTime()
    println "Time taken: ${(end - start) / 1.0e9} Seconds"
  }
}

class InterceptTest{

    def foo(){
        println "foo"
    }
    def bar(){
        println "bar"
    }
    def ex(){
        throw new Exception("exception")
    }
}

InterceptTest.metaClass.invokeMethod = { String name, args ->
    //System.out.println("Call to $name intercepted on $delegate... " )
    def validMethod = InterceptTest.metaClass.getMetaMethod(name, args)
    if (validMethod == null){
        return InterceptTest.metaClass.invokeMissingMethod(delegate, name, args)
    }
    println("running pre-filter... " )
    try{
        result = validMethod.invoke(delegate, args) // Remove this for around-advice
    }
    catch(Exception ex){
        println("name=$name message=${ex.getMessage()}" )
    }
    println("running post-filter... " )
    result
}
def i = new InterceptTest()
Time.code{
    1000.times{
        i.foo()
        i.bar()
        try{
            i.ex()
        }
        catch(Exception ex){
            println("name=ex message=${ex.getMessage()}" )
        }
    }
}