int main ()
    {
    int a :=0;
    int continue_loop := 1 ;
    while ( continue_loop=1 )
    {
        a:=a+1;
        if(a=5)
        {
            print (5);
        }
        else
        {
            print (2);
        }
        if(a =10) {
            continue_loop := 0 ;
        }
    }
    return;
}