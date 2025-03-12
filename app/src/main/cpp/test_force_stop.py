# Test force stop for image processing

def create_stop_file(directory):

    file_path = f"{directory}isForceStopNow.txt"
    
    with open(file_path, 'w') as file:
        file.write('1')
        file.close()

directory_path = "./debug/"
create_stop_file(directory_path)
