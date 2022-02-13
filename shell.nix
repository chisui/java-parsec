{ sources ? import ./nix/sources.nix }:
let pkgs = import sources.nixpkgs { };
in with pkgs;
mkShell {
  name = "parsec";
  buildInputs = [
    maven
    javaPackages.compiler.openjdk8
  ];
}
