# use git clone to get
git clone https://github.com/kvaderlipa/FileEncoder FileEncoder

# then setup alias, java must be installed
# sudo apt install default-jre
alias FileEncoder='java -cp /home/kvaderlipa/FileEncoder/dist/FileEncoder.jar fileEncoder.FileEncoder'

# then add alias at the end of /home/kvaderlipa/.bashrc file
cp /home/kvaderlipa/.bashrc /home/kvaderlipa/.bashrc.bak
echo "alias FileEncoder='java -cp /home/kvaderlipa/FileEncoder/dist/FileEncoder.jar fileEncoder.FileEncoder'" >> /home/kvaderlipa/.bashrc

# to only refresh jar file
wget -O /home/kvaderlipa/FileEncoder/dist/FileEncoder.jar https://github.com/kvaderlipa/FileEncoder/raw/refs/heads/master/dist/FileEncoder.jar