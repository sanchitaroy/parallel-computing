#include <sstream>
#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <fstream>
#include <omp.h>
#include <time.h>

using namespace std;

int main(int argc, char* argv[]){

	std::fstream inpf(argv[1]);
	std::ofstream outfByRank("Output_Task1_SortedByRank.csv");
	std::ofstream outfByNode("Output_Task1.csv");

	int a=0,b=0;
	int maxNode=0;

	while(inpf != NULL)
	{
		inpf>>a>>b;
		if (maxNode<a)
		maxNode=a;
		if(maxNode<b)
		maxNode=b;
	}
	inpf.close();

	maxNode=maxNode+1; //maximum number of nodes

	cout<<"total number of nodes : "<<maxNode<<endl;

	void getInlinkMatrix(int maxN, double **Mat);
	void calcPageRankAndConverge(int maxN, double **Mat, double rMat[], double rMatTemp[]);
	void sortByRank(int maxN, double **Mat);

	double *resultMat  = new double [maxNode];
	
	double *resultMatTemp = new double [maxNode];
	double **adjMat;
	
	double **finalArrSortByRank;
	double **finalArrSortByNode;

	adjMat =(double**)malloc((maxNode)*sizeof(double*));
		for(int p=0;p<(maxNode);p++)
			adjMat[p]=(double*)malloc(maxNode*sizeof(double));

	finalArrSortByRank =(double**)malloc((maxNode)*sizeof(double*));
		for(int p=0;p<(maxNode);p++)
			finalArrSortByRank[p]=(double*)malloc(2*sizeof(double));

	finalArrSortByNode =(double**)malloc((maxNode)*sizeof(double*));
		for(int p=0;p<(maxNode);p++)
			finalArrSortByNode[p]=(double*)malloc(2*sizeof(double));

	//initialize with 0 - do parallely
	#pragma omp parallel for
	for(int z = 0; z<maxNode; z++){
		for(int y = 0; y<maxNode; y++){
			adjMat[z][y] = 0;
		}
	}

	std::fstream inpf2(argv[1]);
	
	//#pragma omp atomic	
	while(!inpf2.eof())
	{
		inpf2>>a>>b;
		adjMat[a][b] =1;
		adjMat[b][a]=1;
	}

	inpf2.close();

	clock_t start, finish;
	start = clock();

	//initialize with 1/n - parallel process
	#pragma omp parallel for	
	for(int i=0; i<maxNode;i++)
	{
		resultMat[i] = 1.0/maxNode;      
		resultMatTemp[i]= 1.0/maxNode;
	}

	getInlinkMatrix(maxNode, adjMat);

	calcPageRankAndConverge(maxNode, adjMat, resultMat, resultMatTemp);

	finish = clock();

	cout<<"Total time taken for calculation : "<<((finish - start)/CLOCKS_PER_SEC)<<" second(s)"<<endl;

	double s = 0;
	for (int z = 0; z<maxNode; z++) {
		finalArrSortByRank[z][0] = z;
		finalArrSortByRank[z][1] = resultMat[z];
		finalArrSortByNode[z][0] = z;
		finalArrSortByNode[z][1] = resultMat[z];
		s+=resultMat[z];
	}

	cout<<"sum of all ranks : "<<s<<endl;

	sortByRank(maxNode, finalArrSortByRank);

	outfByRank<<"Node id "<<"\t"<<"Page Rank"<<"\n";
	outfByNode<<"Node id "<<"\t"<<"Page Rank"<<"\n";
	
	for(int z = 0; z<maxNode; z++){
		outfByRank<<finalArrSortByRank[z][0]<<"\t"<<finalArrSortByRank[z][1]<<"\n";
		outfByNode<<finalArrSortByNode[z][0]<<"\t"<<finalArrSortByNode[z][1]<<"\n";
	}

	outfByRank.close();
	outfByNode.close();
	
	return 0;
}

void getInlinkMatrix(int maxN, double **Mat){
	
	int sum=0;	
	for(int i=0;i<maxN;i++)
	{
		sum=0;
	
		#pragma omp parallel for reduction(+:sum)
		for(int j=0;j<maxN;j++)
		{        
			if(Mat[j][i]!=0)
			sum++;
		}

		#pragma omp parallel for
		for(int j=0;j<maxN;j++) { 
			//using teleport. However, that would not be required for
			//an undirected graph 
			if(Mat[j][i]!=0)
				Mat[j][i]=(1.0/sum)*0.85 + 0.15/maxN;

			else 
				Mat[j][i]=0.15/maxN;	
		}		     
	}

}

void calcPageRankAndConverge(int maxN, double **Mat, double rMat[], double rMatTemp[]){

	int iter=0;
	int flag=0;

	while(true){     
		if(flag==1)
		break;          
		flag=1; 

		int nthreads, tid, i, chunk = 100;
		
		#pragma omp parallel shared(Mat,rMat,rMatTemp,nthreads,chunk) private(i,tid)
		{
			tid = omp_get_thread_num();
			if (tid == 0) {
				nthreads = omp_get_num_threads();
			}

			#pragma omp for schedule(static, chunk)
			for(int k=0;k<maxN;k++)        
			{
				double sum=0;
				for(int m=0;m<maxN;m++)        
				{       
					//multiply the two matrix				
					sum += Mat[k][m] * rMat[m] ;
				}    
				rMatTemp[k] = sum;
			}
		}
  
		//convergence condition
		#pragma omp parallel for		
		for(int m=0;m<maxN;m++)      
		{       
			if(rMat[m]-rMatTemp[m]>0)
				if(rMat[m]-rMatTemp[m]>.00001)
					flag=0;
                     
			if(rMat[m]-rMatTemp[m]<0)
				if(rMatTemp[m]-rMat[m]>.00001)
					flag=0;          
		}
		
		#pragma omp parallel for
		for(int m=0;m<maxN;m++)        
		{                  
			rMat[m]=rMatTemp[m] ; 
		}
    
	iter++;
	} 

	cout<<"number of iterations : "<<iter<<endl;
}

void sortByRank(int maxN, double **Mat){

	int ch;
	int nr = 0;
	double tempKey, tempVal;

	ch = 1;

	while(ch) {
		#pragma omp parallel private(tempKey, tempVal)
		{
			nr++;
			ch = 0;

			#pragma omp for reduction(+:ch)
			for(int i = 0; i < maxN - 1; i = i + 2)
			{
				if(Mat[i][1] < Mat[i+1][1]) { //descending
	
					tempKey = Mat[i][0];    
					tempVal = Mat[i][1];
					Mat[i][1] = Mat[i+1][1];
					Mat[i][0] = Mat[i+1][0];
					Mat[i+1][1] = tempVal;
					Mat[i+1][0] = tempKey;
					++ch;

				}
			}
			#pragma omp for reduction(+:ch)
			for(int i = 1; i < maxN - 1; i = i + 2) {
				if(Mat[i][1] < Mat[i+1][1]) { //descending

					tempKey = Mat[i][0];    
					tempVal = Mat[i][1];
					Mat[i][1] = Mat[i+1][1];
					Mat[i][0] = Mat[i+1][0];
					Mat[i+1][1] = tempVal;
					Mat[i+1][0] = tempKey;
					++ch;
				}
			}
		}
	}
}
