#include <fstream>
#include <sstream>
#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <math.h>
#include <mpi.h>

using namespace std;

int main (int argc, char *argv[])
{
	int i, nprocs, rank, rc;
	void update(int Arr[][2], int size, int maxEl);
	void merge(int Arr1[][2], int Arr2[][2], int sizeOfSubArr, int answer[][2]);

	MPI_Status status;

	/************** initialize *****************/
	MPI_Init (&argc, &argv);
    	MPI_Comm_size (MPI_COMM_WORLD, &nprocs);
    	
	if(nprocs !=4){
		printf("Quitting. Number of MPI tasks must be 4.\n");
   		MPI_Abort(MPI_COMM_WORLD, rc);
   		exit(0);
   	}

	MPI_Comm_rank (MPI_COMM_WORLD, &rank);

	/********************* get number of lines in the file ********************************/
	std::string line;
	int NOL = 0;
	int maxEl = 0;
	int a;

    	std::ifstream myfile("100000_key-value_pairs.csv");

	while (std::getline(myfile, line)){
		++NOL;		
		myfile>>a;
		if (maxEl<a)
			maxEl=a;
	}

	cout<<"max element is :"<<maxEl<<endl;
	int sizeOfSubArr = ceil(NOL/4)+1;

	myfile.close();

	//for multiple arrays
	int ArrMod0[sizeOfSubArr][2];		
	int ArrMod1[sizeOfSubArr][2];
	int ArrMod2[sizeOfSubArr][2];
	int ArrMod3[sizeOfSubArr][2];

	int ArrMerg1[2*sizeOfSubArr][2];		
	int ArrMerg2[2*sizeOfSubArr][2];
	int FinalMergArr[4*sizeOfSubArr][2];

	for(int p=0;p<(4*sizeOfSubArr);p++){
		if(p<2*sizeOfSubArr) {
			ArrMerg1[p][0] = maxEl+1;
			ArrMerg1[p][1] = maxEl+1;
			ArrMerg2[p][0] = maxEl+1;
			ArrMerg2[p][1] = maxEl+1;
		}
		if(p<sizeOfSubArr) {
			ArrMod0[p][0] = maxEl+1;
			ArrMod1[p][0] = maxEl+1;
			ArrMod2[p][0] = maxEl+1;
			ArrMod3[p][0] = maxEl+1;

			ArrMod0[p][1] = maxEl+1;
			ArrMod1[p][1] = maxEl+1;
			ArrMod2[p][1] = maxEl+1;
			ArrMod3[p][1] = maxEl+1;
		}
		FinalMergArr[p][0] = maxEl+1;
		FinalMergArr[p][1] = maxEl+1;
	}
	cout<<"------------------------ end of shared memory allocation --------------------------"<<endl;
	if(rank == 0){

		cout<<"------------------------ processor 0 --------------------------"<<endl;
		double timeMaster, timeSlaveStart, timeSlaveEnd;
		
		//for multiple arrays
		int j = 0;
		int k = 0;
		int l = 0;
		int m = 0;

		timeMaster = MPI_Wtime();

		std::ifstream file("100000_key-value_pairs.csv");

		    for(int row = 0; row < NOL; ++row)
		    {
			std::string line;
			std::getline(file, line);
			if ( !file.good() )
			    break;

			std::stringstream iss(line);

			for (int col = 0; col < 2; ++col)
			{
			    std::string val;
			    std::getline(iss, val, ',');
			    std::stringstream convertor(val);

			    if(abs(row%4) == 0){
				convertor >> ArrMod0[j][col];
				if(col ==1)
					j++;
			    }
			    else if(abs(row%4) == 1){
				convertor >> ArrMod1[k][col];
				if(col ==1) k++;
			    }
			    else if(abs(row%4) == 2){
				convertor >> ArrMod2[l][col];
				if(col ==1) l++;
			    }
			    else{
				convertor >> ArrMod3[m][col];
				if(col ==1) m++;
			    }
			}
		    }
		file.close();

		cout<<"Work Distribution starts"<<endl;
		timeSlaveStart = MPI_Wtime();

		MPI_Send(&ArrMod1,sizeOfSubArr*2,MPI_INT,1,1,MPI_COMM_WORLD);
		MPI_Send(&ArrMod2,sizeOfSubArr*2,MPI_INT,2,2,MPI_COMM_WORLD);
		MPI_Send(&ArrMod3,sizeOfSubArr*2,MPI_INT,3,3,MPI_COMM_WORLD);

		update(ArrMod0, sizeOfSubArr, maxEl);
		cout<<"------------------------ end initial work : processor 0 --------------------------"<<endl;
		MPI_Send(&ArrMod0,sizeOfSubArr*2,MPI_INT,1,4,MPI_COMM_WORLD);

		MPI_Recv(&ArrMerg1,(2*sizeOfSubArr)*2,MPI_INT,1,5,MPI_COMM_WORLD,&status);
		MPI_Recv(&ArrMerg2,(2*sizeOfSubArr)*2,MPI_INT,3,6,MPI_COMM_WORLD,&status);

		merge(ArrMerg1, ArrMerg2, 2*sizeOfSubArr, FinalMergArr);

		cout<<"Work Distribution ends"<<endl;
		timeSlaveEnd = MPI_Wtime();
		std::ofstream outf("Output_Task2.txt");
		for(int z = 0; z<4*sizeOfSubArr; z++){
			if(FinalMergArr[z][0] >=maxEl+1)
				continue;
			outf<<FinalMergArr[z][0]<<","<<FinalMergArr[z][1]<<"\n";
			
		}
		outf.close();	
		cout<<"Total time taken : "<<MPI_Wtime()-timeMaster<<endl;
		cout<<"Total time taken in parallel task : "<<timeSlaveEnd-timeSlaveStart<<endl;
		cout<<"------------------------ end work : processor 0 --------------------------"<<endl;
	}
	else if(rank == 1){
		cout<<"------------------------ processor 1 --------------------------"<<endl;
		MPI_Recv(&ArrMod1,sizeOfSubArr*2,MPI_INT,0,1,MPI_COMM_WORLD,&status);
					
		update(ArrMod1, sizeOfSubArr, maxEl);
		cout<<"------------------------ end initial work : processor 1 --------------------------"<<endl;
		MPI_Recv(&ArrMod0,sizeOfSubArr*2,MPI_INT,0,4,MPI_COMM_WORLD,&status);
		merge(ArrMod0, ArrMod1, sizeOfSubArr, ArrMerg1);

		MPI_Send(&ArrMerg1,(2*sizeOfSubArr)*2,MPI_INT,0,5,MPI_COMM_WORLD);
		cout<<"------------------------ end work : processor 1 --------------------------"<<endl;
	}
	else if(rank == 2){
		cout<<"------------------------ processor 2 --------------------------"<<endl;
		MPI_Recv(&ArrMod2,sizeOfSubArr*2,MPI_INT,0,2,MPI_COMM_WORLD,&status);		
		update(ArrMod2, sizeOfSubArr, maxEl);		
		MPI_Send(&ArrMod2,sizeOfSubArr*2,MPI_INT,3,9,MPI_COMM_WORLD);
		cout<<"------------------------ end work : processor 2 --------------------------"<<endl;
	}
	else{
		cout<<"------------------------ processor 3 --------------------------"<<endl;	
		MPI_Recv(&ArrMod3,sizeOfSubArr*2,MPI_INT,0,3,MPI_COMM_WORLD,&status);				
		update(ArrMod3, sizeOfSubArr, maxEl);
		cout<<"------------------------ end initial work : processor 3 --------------------------"<<endl;		
		MPI_Recv(&ArrMod2,sizeOfSubArr*2,MPI_INT,2,9,MPI_COMM_WORLD,&status);
		merge(ArrMod2, ArrMod3, sizeOfSubArr, ArrMerg2);

		MPI_Send(&ArrMerg2,(2*sizeOfSubArr)*2,MPI_INT,0,6,MPI_COMM_WORLD);
		cout<<"------------------------ end work : processor 3 --------------------------"<<endl;
	}
	
	MPI_Finalize();
	return 0;
}

void update(int Arr[][2], int size, int maxEl){
	int key = 0;
	int sum = 0;
	int cNonRepeat = 0;
	int countNonRepeat = 0;
	int tempKey = 0;
	int tempVal = 0;

	for(int i = 0; i<size; i++){
		key = Arr[i][0];
		if(key == maxEl+1){
			countNonRepeat++; 
			continue;
		}
		sum = Arr[i][1];
		for(int j=i+1; j<size; j++){
			if(key == Arr[j][0]){
				sum = sum + Arr[j][1];	
				Arr[j][0] = maxEl+1;
				Arr[j][1] = maxEl+1;
			}		
		}
		Arr[i][1] = sum;
	}

	//perform bubble sort
	for (int c = 0 ; c < ( size); c++) {
		for (int d = 0 ; d < size - c - 1; d++) {
			if (Arr[d][0] > Arr[d+1][0]) //ascending
			{
				tempKey=Arr[d][0]; //swap
				tempVal=Arr[d][1]; 
				Arr[d][0]   = Arr[d+1][0];
				Arr[d][1]   = Arr[d+1][1];
				Arr[d+1][0] = tempKey;
				Arr[d+1][1] = tempVal;
			}
		}
	  }
}

void merge(int Arr1[][2], int Arr2[][2], int sizeOfSubArr, int answer[][2]){

	int i = 0, j = 0, k = 0;

	while (i < sizeOfSubArr && j < sizeOfSubArr){
		if (Arr1[i][0] < Arr2[j][0]) {
			answer[k][0] = Arr1[i][0];
			answer[k][1] = Arr1[i][1];
			i++;
		}
		else if(Arr1[i][0] == Arr2[j][0]){
			answer[k][0] = Arr1[i][0];
			answer[k][1] = Arr1[i][1]+Arr2[j][1];
			i++;
			j++;
		}
		else {
			answer[k][0] = Arr2[j][0];
			answer[k][1] = Arr2[j][1];
			j++;
		}
		k++;
	}

	while (i < sizeOfSubArr) {
		answer[k][0] = Arr1[i][0];
		answer[k][1] = Arr1[i][1];
		i++;
		k++;
	}

	while (j < sizeOfSubArr){
		answer[k][0] = Arr2[j][0];
		answer[k][1] = Arr2[j][1];
		j++;
		k++;
	}
}
