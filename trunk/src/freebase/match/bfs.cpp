#include <stdio.h>


const int NMAX=100000000;
const int MAX = 10000;

int *ngraph[NMAX];
int *egraph[NMAX];
int gsize[NMAX],gnitem[NMAX];
char line[MAX];

void insert(int arg1, int arg2, int relid){
  if(gnitem[arg1] == gsize[arg1]){
    gsize[arg1]*=2;
    ngraph[arg1] = (int *)realloc(ngraph[arg1],size*sizeof(int));
    egraph[arg1] = (int *)realloc(egraph[arg1],size*sizeof(int));
  }
  ngraph[gnitem[arg1]] = arg2;
  egraph[gnitem[arg1]]=relid;
  gnitem[arg1]++;
}


void load_graph(char *graph_file){
  FILE *fp = fopen(graph_file);
  for(int i=0;i<NMAX;i++){
    gsize[i]=10;
    gnitem[i] = 0;
    graph[i] = (int *)malloc(gsize[i]*sizeof(int));
  }
  int count = 0;
  for(;fgets(line,MAX,fp);count++){
    char *arg1str = strtok(line," \t");
    int arg1 = atoi(arg1str);
    char *relstr = strtok(NULL, " \t");
    int rel = atoi(relstr);
    char *arg2str = strtok(NULL, " \t");
    int arg2 = atoi(arg2str);
    insert(arg1,arg2,rel);
    insert(arg2,arg2,-1*rel);
  }
  fclose(fp);
}
int main(int argc, char* argv[]){
  load_graph("");
  return 0;
}

