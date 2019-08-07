Overview
This project is a secure intro workflow using AppRole pull mechanism. We are using packer to embed role-id in our pre-baked image. We are using a CM tool (SaltStack) to deliver the secret and bootstrap the process.

I've included the source code for the spring boot Java app if you want to build the java app and packer image yourself. You will need to provide your own OVA in this directory if you take this approach. I built my image from this vagrant box: https://app.vagrantup.com/bento/boxes/ubuntu-16.04. My box is availability publicly on Vagrant cloud for use: `lanceplarsen/bento-ubuntu-1604-springboot`. The role-id is hardcoded and the Java app is packaged in my image for use.

Instructions for use:

1. Configure SaltStack with `vagrant up saltmaster`.
2. Configure Vault with `vagrant up vault`.
3. Configure Springboot with `vagrant up springboot`.
4. SSH to the Saltmaster and run the Springboot state as root with `salt 'springboot' state.apply`.
5. Check the embedded Tomcat server on localhost:8080/. You'll see the Vault secret.
