terraform {
required_version = ">= 0.14.0"
  required_providers {
    openstack = {
      source  = "terraform-provider-openstack/openstack"
      version = "~> 1.48.0"
    }
  }
}

provider "openstack" {
  user_name   = "benjamin.lemaire.etu"
  password    = "Uj!9DmP4$31#gj@pO0"
  auth_url    = "http://cloud.univ-lille.fr:5000/v3/"
  region      = "RegionOne"
}

resource "openstack_compute_instance_v2" "dal-banque-preprod" {
    count = 1
    name = "dal-banque-preprod"
    provider = openstack
    image_name = "ubuntu-22.04"
    flavor_name = "moyenne"
    key_pair = "ordi-perso"
}

resource "openstack_compute_instance_v2" "dal-banque-preprod" {
    count = 1
    name = "dal-banque-prod"
    provider = openstack
    image_name = "ubuntu-22.04"
    flavor_name = "moyenne"
    key_pair = "ordi-perso"
}
