class Light {
  constructor(name, state) {
    this.name = name;
    this.state = state;
    if(state == "emergency mode") {
      this.color = "orange"
    } else {
      this.color = state;
    }
  }
}
