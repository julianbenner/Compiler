int pow(int a, int b) {
    int c:=1;
    while(b>0) {
        c:=c*a;
        b:=b-1;
    }
    return c;
}

int main () {
    int i:=2;
    int j:=16;
    print(i . " hoch " . j . " ist " . pow(i,j) . ", oder auch " . i . "^" . j . "=" . pow(i,j));
    return 0;
}