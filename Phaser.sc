Phaser1 {

	*ar { arg input, mix = 0.5, depth = 1, fb = 0.3, cfb = 0.1, poles = 6;
		var output, feedback, ac, fArray;

		// compute allpass coefficient
		ac = { |freq|
			var theta = pi*SampleDur.ir*freq;
			var a1 = (1 - tan(theta))/(1 + tan(theta));
			a1;
		};

		fArray = Array.fill2D(poles,2, {|i,j|
			var div = ((i+1) / (poles*2)) + 0.5;
			var logMin = 70.log;
			var logMax = 18050.log;
	((div * ((logMax - logMin) + logMin)).exp)/(1+(9*(1-j)));
		});

		feedback = LocalIn.ar(2);
		output = input + (feedback*fb) + (feedback.reverse*cfb);
		fArray do: { |freqs|
			var a1 = ac.(freqs[0] + ((freqs[1] - freqs[0])*depth));

			output = FOS.ar(output, a1, -1, a1);   // 1st order allpass
		};
		output = mix*output;
		LocalOut.ar(output);

		^((1 - mix)*input + output);
	}
///find a way to make depth exponential.
}

Phaser2 {

	*ar { arg input, mix = 0.5, depth = 1, fb = 0.3, cfb = 0.1, rq = 1, poles = 6;
		var output, feedback, ac, fArray;

		// compute allpass coefficient
		ac = { |freq, rq|
			var a2 = 1 - (pi*(freq/SampleRate.ir)*rq);
			var a1 = -2*a2*cos(freq*2pi*SampleDur.ir);
			a2 = a2.squared;
			[a1, a2];
		};

		fArray = Array.fill2D(poles,2, {|i,j|
			var div = ((i+1) / (poles*2)) + 0.5;
			var logMin = 70.log;
			var logMax = 18050.log;
	((div * ((logMax - logMin) + logMin)).exp)/(1+(9*(1-j)));
		});


		feedback = LocalIn.ar(2);
		output = input + (feedback*fb) + (feedback.reverse*cfb);
		fArray do: { |freqs|
			var a1, a2;
			#a1, a2 = ac.(freqs[0] + ((freqs[1] - freqs[0])*depth), rq);
			// smooth time-varying coeffs with a 1-pole lowpass (doesn't seem like we need it though...)
			//#a1, a2 = [a1, a2] collect: { |c| FOS.ar(c, 1 - 0.997, 0, 0.997) };
			output = SOS.ar(output, a2, a1, 1, a1.neg, a2.neg);
		};
		output = mix*output;
		LocalOut.ar(output);

		^((1 - mix)*input + output);
	}

}
