use std::error::Error;
use std::thread::{self};
use std::time::Duration;

use rppal::gpio::Gpio;
use rppal::system::DeviceInfo;

// Gpio uses BCM pin numbering. BCM GPIO 23 is tied to physical pin 16.
const GPIO_MIC: u8 = 7;

fn main() -> Result<(), Box<dyn Error>> {
    let mut pin = Gpio::new()?.get(GPIO_MIC)?.into_output();

    // Blink the LED by setting the pin's logic level high for 500 ms.
    loop {
        println!("Blinking an LED on a {}.", DeviceInfo::new()?.model());
        pin.set_high();
        thread::sleep(Duration::from_millis(500));
        pin.set_low();
    }
}
