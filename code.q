int main () {
    int a :=0;
    bool continue_loop := true ;
    while ( continue_loop ) {
        a:=a+1;
        if(a=5) {
            print (a);
        } else {
            print (0);
        }
        if(a =10)
        continue_loop := false ;
    }
    return 0;
}